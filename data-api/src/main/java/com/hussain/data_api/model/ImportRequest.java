package com.hussain.data_api.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportRequest {
    private String entity;
    private List<Map<String, Object>> records;
    private ImportMode mode;
    private String batchSize;
    private boolean validateOnly;
    private Map<String, String> columnMapping;
    
    public enum ImportMode {
        INSERT, UPDATE, UPSERT, REPLACE
    }
}