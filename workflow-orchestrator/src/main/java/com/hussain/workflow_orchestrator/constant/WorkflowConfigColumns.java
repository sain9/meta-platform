package com.hussain.workflow_orchestrator.constant;

/**
 * Column name constants for the workflow_config table.
 * Single source of truth — referenced by repositories, mappers, and services.
 */
public final class WorkflowConfigColumns {

    private WorkflowConfigColumns() {}

    public static final String TABLE = "workflow_config";

    public static final String ID = "id";
    public static final String WORKFLOW_NAME = "workflow_name";
    public static final String DESCRIPTION = "description";
    public static final String DATA_QUERY = "data_query";
    public static final String SOURCE_SINK_CONFIG = "source_sink_config";
    public static final String EXECUTION_CONFIG = "execution_config";
    public static final String ENABLED = "enabled";
    public static final String CREATED_BY = "created_by";
    public static final String CREATED_AT = "created_at";
    public static final String UPDATED_AT = "updated_at";
}