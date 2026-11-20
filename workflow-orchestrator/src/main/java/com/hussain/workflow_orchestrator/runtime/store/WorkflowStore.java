package com.hussain.workflow_orchestrator.runtime.store;

import com.hussain.workflow_orchestrator.domain.CachedWorkflow;
import com.hussain.workflow_orchestrator.util.HashUtil;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Primary runtime cache — mirrors Spriced's WorkflowStore.
 *   byName      : lookup for POST /workflow/{name}
 *   byId        : lookup for scheduled runs, audit linkage
 *   checksums   : MD5 of serialized config, for change detection
 */
@Component
public class WorkflowStore {

    private static final Logger log = LoggerFactory.getLogger(WorkflowStore.class);

    private final ConcurrentMap<String, CachedWorkflow> byName = new ConcurrentHashMap<>();
    private final ConcurrentMap<UUID, CachedWorkflow> byId = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, String> checksums = new ConcurrentHashMap<>();

    /**
     * Change detection. Returns true if new content differs from last-seen checksum.
     * Caller uses the return value to decide whether to reprocess the workflow.
     */
    public boolean updateIfChanged(String name, String serializedContent) {
        String newHash = HashUtil.md5(serializedContent);
        String oldHash = checksums.get(name);
        if (newHash == null ? oldHash == null : newHash.equals(oldHash)) {
            return false;
        }
        checksums.put(name, newHash);
        return true;
    }

    public void upsert(CachedWorkflow workflow) {
        byName.put(workflow.name(), workflow);
        byId.put(workflow.id(), workflow);
    }

    public void remove(String name) {
        CachedWorkflow removed = byName.remove(name);
        if (removed != null) {
            byId.remove(removed.id());
            checksums.remove(name);
            log.debug("Removed from cache: {} ({})", name, removed.id());
        }
    }

    public CachedWorkflow get(String name) {
        return byName.get(name);
    }

    public CachedWorkflow getById(UUID id) {
        return byId.get(id);
    }

    public Set<String> listWorkflowNames() {
        return Set.copyOf(byName.keySet());
    }

    public Collection<CachedWorkflow> all() {
        return List.copyOf(byName.values());
    }

    public int size() {
        return byName.size();
    }

    public void clear() {
        byName.clear();
        byId.clear();
        checksums.clear();
    }
}