package com.hussain.workflow_orchestrator.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hussain.workflow_orchestrator.config.WorkflowStoreProperties;
import com.hussain.workflow_orchestrator.domain.CachedWorkflow;
import com.hussain.workflow_orchestrator.domain.WorkflowConfig;
import com.hussain.workflow_orchestrator.health.WorkflowOrchestratorHealthIndicator;
import com.hussain.workflow_orchestrator.metrics.WorkflowMetrics;
import com.hussain.workflow_orchestrator.repository.WorkflowConfigRepository;
import com.hussain.workflow_orchestrator.runtime.loader.WorkflowConfigLoader;
import com.hussain.workflow_orchestrator.runtime.scheduler.WorkflowSchedulerArmer;
import com.hussain.workflow_orchestrator.runtime.store.WorkflowStore;
import com.hussain.workflow_orchestrator.util.ExecutionConfigParser;
import jakarta.annotation.PostConstruct;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class WorkflowService {

    private static final Logger log = LoggerFactory.getLogger(WorkflowService.class);

    private final WorkflowConfigRepository repository;
    private final WorkflowStore workflowStore;
    private final WorkflowConfigLoader configLoader;
    private final WorkflowSchedulerArmer schedulerArmer;
    private final ExecutionConfigParser executionParser;
    private final WorkflowStoreProperties props;
    private final ObjectMapper objectMapper;
    private final WorkflowMetrics metrics;
    private final WorkflowOrchestratorHealthIndicator health;

    public WorkflowService(
            WorkflowConfigRepository repository,
            WorkflowStore workflowStore,
            WorkflowConfigLoader configLoader,
            WorkflowSchedulerArmer schedulerArmer,
            ExecutionConfigParser executionParser,
            WorkflowStoreProperties props,
            ObjectMapper objectMapper,
            WorkflowMetrics metrics,
            WorkflowOrchestratorHealthIndicator health) {
        this.repository = repository;
        this.workflowStore = workflowStore;
        this.configLoader = configLoader;
        this.schedulerArmer = schedulerArmer;
        this.executionParser = executionParser;
        this.props = props;
        this.objectMapper = objectMapper;
        this.metrics = metrics;
        this.health = health;
    }

    @PostConstruct
    public void initOrchestrator() {
        log.info("=== WorkflowService warmup starting ===");
        refreshWorkflowCache();
        log.info("=== WorkflowService warmup complete — {} workflow(s) in cache ===",
                workflowStore.size());
    }

    @Scheduled(fixedDelayString = "${workflow.store.poll-interval-ms:60000}")
    public void refreshWorkflowCache() {
        if (!props.enabled()) {
            log.debug("[poll] workflow.store.enabled=false — skipping");
            return;
        }
        long t0 = System.currentTimeMillis();
        try {
            log.debug("[poll] refreshWorkflowCache() start");

            List<WorkflowConfig> active = repository.findAllEnabled();
            Set<String> cachedNames = workflowStore.listWorkflowNames();
            Set<String> dbNames = active.stream()
                    .map(WorkflowConfig::workflowName)
                    .collect(Collectors.toSet());

            log.debug("[poll] DB rows={}, cached={}, dbNames={}, cachedNames={}",
                    active.size(), workflowStore.size(), dbNames, cachedNames);

            Set<String> toRemove = cachedNames.stream()
                    .filter(n -> !dbNames.contains(n))
                    .collect(Collectors.toSet());
            if (!toRemove.isEmpty()) {
                for (String name : toRemove) {
                    CachedWorkflow before = workflowStore.get(name);
                    workflowStore.remove(name);
                    log.info("[poll][REMOVED] workflow '{}' (id={}) — no longer enabled/present in DB",
                            name, before != null ? before.id() : "?");
                }
            }

            int added = 0, updated = 0, unchanged = 0;
            for (WorkflowConfig cfg : active) {
                String serialized = serialize(cfg);
                boolean changed = workflowStore.updateIfChanged(cfg.workflowName(), serialized);
                boolean existed = workflowStore.get(cfg.workflowName()) != null;

                if (!changed) {
                    unchanged++;
                    log.debug("[poll][SAME] workflow '{}' unchanged", cfg.workflowName());
                    continue;
                }

                if (processWorkflowConfig(cfg)) {
                    if (existed) {
                        updated++;
                        log.info("[poll][UPDATED] workflow '{}' (id={})", cfg.workflowName(), cfg.id());
                    } else {
                        added++;
                        log.info("[poll][NEW] workflow '{}' (id={})", cfg.workflowName(), cfg.id());
                    }
                    log.info("[poll]   full workflow_config: {}", serialized);
                }
            }

            if (added > 0 || updated > 0 || !toRemove.isEmpty()) {
                configLoader.reload();
            }

            schedulerArmer.reconcile(workflowStore.all());

            dumpCacheSnapshot();

            long elapsed = System.currentTimeMillis() - t0;
            log.debug("[poll] done in {}ms — added={}, updated={}, removed={}, unchanged={}, cache size={}",
                    elapsed, added, updated, toRemove.size(), unchanged, workflowStore.size());

            metrics.recordPoll(true, elapsed);
            health.markPollSuccess();

        } catch (Exception ex) {
            long elapsed = System.currentTimeMillis() - t0;
            log.error("[poll] refreshWorkflowCache() FAILED; will retry next tick", ex);
            metrics.recordPoll(false, elapsed);
            health.markPollFailure(ex.getClass().getSimpleName() + ": " + ex.getMessage());
        }
    }

    public boolean ensureWorkflowLoaded(String workflowName) {
        if (workflowStore.get(workflowName) != null) {
            return true;
        }
        log.info("[ensure] workflow '{}' not cached — forcing immediate refresh", workflowName);
        refreshWorkflowCache();
        return workflowStore.get(workflowName) != null;
    }

    public CachedWorkflow findCached(String name) {
        return workflowStore.get(name);
    }

    private void dumpCacheSnapshot() {
        var all = workflowStore.all().stream()
                .sorted(Comparator.comparing(CachedWorkflow::name))
                .toList();

        if (all.isEmpty()) {
            log.info("[cache] CACHE SNAPSHOT — empty");
            return;
        }
        log.info("[cache] CACHE SNAPSHOT — {} workflow(s):", all.size());
        for (CachedWorkflow wf : all) {
            log.info("[cache]   '{}' id={} type={} enabled={} config={}",
                    wf.name(), wf.id(), wf.executionType(),
                    wf.config().enabled(), serialize(wf.config()));
        }
    }

    private boolean processWorkflowConfig(WorkflowConfig cfg) {
        try {
            CachedWorkflow cached = new CachedWorkflow(
                    cfg.id(),
                    cfg.workflowName(),
                    cfg,
                    executionParser.parse(cfg.executionConfig()),
                    OffsetDateTime.now());
            workflowStore.upsert(cached);
            return true;
        } catch (IllegalArgumentException invalidConfig) {
            log.error("[poll] INVALID execution_config for workflow '{}' — skipped: {}",
                    cfg.workflowName(), invalidConfig.getMessage());
            return false;
        }
    }

    private String serialize(WorkflowConfig cfg) {
        try {
            return objectMapper.writeValueAsString(cfg);
        } catch (Exception ex) {
            log.warn("[poll] failed to serialize workflow '{}' — using fallback",
                    cfg.workflowName());
            return String.valueOf(System.identityHashCode(cfg)) + ":" + cfg.updatedAt();
        }
    }
}