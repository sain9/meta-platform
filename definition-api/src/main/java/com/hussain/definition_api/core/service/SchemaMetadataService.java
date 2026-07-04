package com.hussain.definition_api.core.service;

import com.hussain.definition_api.core.model.SchemaInfo;

import java.util.List;

public interface SchemaMetadataService {

    SchemaInfo getTableInfo(String schema, String tableName);

    List<SchemaInfo> listTables(String schema);

    List<String> listSchemas();
}