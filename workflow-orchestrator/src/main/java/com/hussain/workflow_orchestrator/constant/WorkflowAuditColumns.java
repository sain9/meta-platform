package com.hussain.workflow_orchestrator.constant;

/**
 * Column name constants for the workflow_audit table.
 */
public final class WorkflowAuditColumns {

    private WorkflowAuditColumns() {}

    public static final String TABLE = "workflow_audit";

    public static final String ID = "id";
    public static final String WORKFLOW_ID = "workflow_id";
    public static final String STATUS = "status";
    public static final String TRIGGER_TYPE = "trigger_type";
    public static final String STARTED_AT = "started_at";
    public static final String COMPLETED_AT = "completed_at";
    public static final String EXECUTION_METRICS = "execution_metrics";
    public static final String EXECUTION_DETAILS = "execution_details";
    public static final String ERROR_MESSAGE = "error_message";
    public static final String CREATED_AT = "created_at";
}