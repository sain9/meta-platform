package com.hussain.workflow_orchestrator.runtime.loader;

import com.hussain.workflow_orchestrator.domain.CachedWorkflow;
import com.hussain.workflow_orchestrator.runtime.store.WorkflowStore;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Secondary execution-time cache — mirrors Spriced's WorkflowConfigLoader.
 * Rebuilds an immutable snapshot from WorkflowStore when the primary cache changes.
 * Executors read from here to never see a partially-built map.
 */
@Component
public class WorkflowConfigLoader {

    private static final Logger log = LoggerFactory.getLogger(WorkflowConfigLoader.class);

    private final WorkflowStore workflowStore;
    private final AtomicReference<Map<String, CachedWorkflow>> snapshot =
            new AtomicReference<>(Map.of());

    public WorkflowConfigLoader(WorkflowStore workflowStore) {
        this.workflowStore = workflowStore;
    }

    public Map<String, CachedWorkflow> getAllConfigs() {
        return snapshot.get();
    }

    public CachedWorkflow get(String name) {
        return snapshot.get().get(name);
    }

    /** Rebuild from WorkflowStore — called by poller when something changed. */
    public void reload() {
        Map<String, CachedWorkflow> fresh = new HashMap<>();
        for (CachedWorkflow wf : workflowStore.all()) {
            fresh.put(wf.name(), wf);
        }
        snapshot.set(Map.copyOf(fresh));
        log.info("WorkflowConfigLoader snapshot reloaded: {} workflow(s)", fresh.size());
    }
}