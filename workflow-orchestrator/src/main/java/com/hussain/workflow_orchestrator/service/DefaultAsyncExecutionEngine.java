package com.hussain.workflow_orchestrator.service;

import com.hussain.workflow_orchestrator.config.ExecutionProperties;
import com.hussain.workflow_orchestrator.domain.CachedWorkflow;
import com.hussain.workflow_orchestrator.domain.ExecutionConfig;
import com.hussain.workflow_orchestrator.domain.enums.AuditStatus;
import com.hussain.workflow_orchestrator.domain.enums.TriggerType;
import com.hussain.workflow_orchestrator.metrics.WorkflowMetrics;
import com.hussain.workflow_orchestrator.repository.WorkflowAuditRepository;
import jakarta.annotation.PreDestroy;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class DefaultAsyncExecutionEngine implements AsyncExecutionEngine {

    private static final Logger log = LoggerFactory.getLogger(DefaultAsyncExecutionEngine.class);

    private final WorkflowAuditRepository auditRepo;
    private final WorkflowExecutor workflowExecutor;
    private final ExecutionProperties props;
    private final WorkflowMetrics metrics;
    private final ThreadPoolExecutor pool;

    public DefaultAsyncExecutionEngine(
            WorkflowAuditRepository auditRepo,
            WorkflowExecutor workflowExecutor,
            ExecutionProperties props,
            WorkflowMetrics metrics) {
        this.auditRepo = auditRepo;
        this.workflowExecutor = workflowExecutor;
        this.props = props;
        this.metrics = metrics;

        int poolSize = props.maxConcurrentWorkflows();
        int queueSize = props.queueCapacity();
        AtomicInteger threadCounter = new AtomicInteger(0);

        this.pool = new ThreadPoolExecutor(
                poolSize, poolSize,
                60L, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(queueSize),
                r -> {
                    Thread t = new Thread(r, "wf-exec-" + threadCounter.incrementAndGet());
                    t.setDaemon(true);
                    return t;
                },
                new ThreadPoolExecutor.AbortPolicy());

        log.info("[engine] initialized — poolSize={}, queueCapacity={}, globalTimeout={}ms",
                poolSize, queueSize, props.globalTimeoutMillis());
    }

    public int activeCount() {
        return pool.getActiveCount();
    }

    public int queueSize() {
        return pool.getQueue().size();
    }

    @Override
    public void submit(CachedWorkflow workflow, UUID auditId, TriggerType triggerType) {
        try {
            pool.execute(() -> run(workflow, auditId, triggerType));
            log.debug("[engine] submitted workflow '{}' (auditId={}, trigger={}), active={}, queue={}",
                    workflow.name(), auditId, triggerType,
                    pool.getActiveCount(), pool.getQueue().size());
        } catch (java.util.concurrent.RejectedExecutionException ex) {
            log.warn("[engine] QUEUE FULL — rejected workflow '{}' (auditId={}), active={}, queue={}",
                    workflow.name(), auditId, pool.getActiveCount(), pool.getQueue().size());
            auditRepo.updateStatus(auditId, AuditStatus.FAILED,
                    "engine queue full (capacity=" + props.queueCapacity() + ")",
                    null, OffsetDateTime.now(), null, null);
            metrics.recordRun(triggerType, AuditStatus.FAILED);
            throw new QueueFullException(
                    "execution queue is full (capacity=" + props.queueCapacity() + ")");
        }
    }

    private void run(CachedWorkflow workflow, UUID auditId, TriggerType triggerType) {
        ExecutionConfig cfg = workflow.executionConfig();
        long timeoutMillis = (cfg != null && cfg.timeoutMillis() != null)
                ? cfg.timeoutMillis()
                : props.globalTimeoutMillis();

        OffsetDateTime startedAt = OffsetDateTime.now();
        log.info("[engine] START '{}' (auditId={}, trigger={}, timeout={}ms)",
                workflow.name(), auditId, triggerType, timeoutMillis);

        Thread worker = Thread.currentThread();
        Thread watchdog = new Thread(() -> {
            try {
                Thread.sleep(timeoutMillis);
                log.warn("[engine] TIMEOUT '{}' (auditId={}) after {}ms — interrupting",
                        workflow.name(), auditId, timeoutMillis);
                worker.interrupt();
            } catch (InterruptedException ignored) { /* worker finished */ }
        }, "wf-watchdog-" + auditId);
        watchdog.setDaemon(true);
        watchdog.start();

        try {
            workflowExecutor.execute(workflow, auditId);
            watchdog.interrupt();

            OffsetDateTime completedAt = OffsetDateTime.now();
            long durationMs = Duration.between(startedAt, completedAt).toMillis();

            auditRepo.updateStatus(auditId, AuditStatus.SUCCESS,
                    null, startedAt, completedAt, null, null);
            metrics.recordRun(triggerType, AuditStatus.SUCCESS);
            metrics.recordRunDuration(triggerType, AuditStatus.SUCCESS, durationMs);
            log.info("[engine] SUCCESS '{}' (auditId={}) in {}ms",
                    workflow.name(), auditId, durationMs);

        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            OffsetDateTime completedAt = OffsetDateTime.now();
            long durationMs = Duration.between(startedAt, completedAt).toMillis();

            auditRepo.updateStatus(auditId, AuditStatus.TIMED_OUT,
                    "exceeded timeout of " + timeoutMillis + "ms",
                    startedAt, completedAt, null, null);
            metrics.recordRun(triggerType, AuditStatus.TIMED_OUT);
            metrics.recordRunDuration(triggerType, AuditStatus.TIMED_OUT, durationMs);
            log.warn("[engine] TIMED_OUT '{}' (auditId={}) after {}ms",
                    workflow.name(), auditId, timeoutMillis);

        } catch (Exception ex) {
            watchdog.interrupt();
            OffsetDateTime completedAt = OffsetDateTime.now();
            long durationMs = Duration.between(startedAt, completedAt).toMillis();

            auditRepo.updateStatus(auditId, AuditStatus.FAILED,
                    ex.getClass().getSimpleName() + ": " + ex.getMessage(),
                    startedAt, completedAt, null, null);
            metrics.recordRun(triggerType, AuditStatus.FAILED);
            metrics.recordRunDuration(triggerType, AuditStatus.FAILED, durationMs);
            log.error("[engine] FAILED '{}' (auditId={}): {}",
                    workflow.name(), auditId, ex.getMessage(), ex);
        }
    }

    @PreDestroy
    void shutdown() {
        log.info("[engine] shutting down — active={}, queue={}",
                pool.getActiveCount(), pool.getQueue().size());
        pool.shutdown();
        try {
            if (!pool.awaitTermination(10, TimeUnit.SECONDS)) {
                log.warn("[engine] forcing shutdown — {} task(s) did not complete",
                        pool.getQueue().size());
                pool.shutdownNow();
            }
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            pool.shutdownNow();
        }
        log.info("[engine] shutdown complete");
    }
}