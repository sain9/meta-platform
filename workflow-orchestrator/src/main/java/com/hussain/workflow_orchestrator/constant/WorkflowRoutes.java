package com.hussain.workflow_orchestrator.constant;

public final class WorkflowRoutes {

    private WorkflowRoutes() {}

    public static final String WORKFLOW_BASE = "/workflow";
    public static final String TRIGGER_BY_NAME = WORKFLOW_BASE + "/{workflowName}";
}