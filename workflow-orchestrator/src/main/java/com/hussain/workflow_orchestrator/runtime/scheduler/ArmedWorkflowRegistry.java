package com.hussain.workflow_orchestrator.runtime.scheduler;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ArmedWorkflowRegistry {

    private static final Logger log = LoggerFactory.getLogger(ArmedWorkflowRegistry.class);

    private final ConcurrentMap<String, ScheduledFuture<?>> futures = new ConcurrentHashMap<>();

    public void put(String workflowName, ScheduledFuture<?> future) {
        ScheduledFuture<?> previous = futures.put(workflowName, future);
        if (previous != null && !previous.isCancelled()) {
            previous.cancel(false);
            log.info("[scheduler][REARMED] '{}' — previous future cancelled", workflowName);
        }
    }

    public Optional<ScheduledFuture<?>> get(String workflowName) {
        return Optional.ofNullable(futures.get(workflowName));
    }

    public void remove(String workflowName) {
        ScheduledFuture<?> removed = futures.remove(workflowName);
        if (removed != null) {
            boolean wasCancelled = removed.cancel(false);
            log.info("[scheduler][DISARMED] '{}' — future cancelled={}", workflowName, wasCancelled);
        }
    }

    public Set<String> armedNames() {
        return Set.copyOf(futures.keySet());
    }

    public boolean isArmed(String workflowName) {
        ScheduledFuture<?> f = futures.get(workflowName);
        return f != null && !f.isCancelled() && !f.isDone();
    }

    public void cancelAll() {
        List<String> names = List.copyOf(futures.keySet());
        for (String name : names) {
            remove(name);
        }
        log.info("[scheduler][SHUTDOWN] cancelled {} armed workflow(s)", names.size());
    }

    public Map<String, ScheduledFuture<?>> snapshot() {
        return Map.copyOf(futures);
    }
}