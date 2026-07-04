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
public class QueryResult {
    private List<Map<String, Object>> records;
    private long totalCount;
    private int affectedRows;
    private String query;
    private Map<String, Object> parameters;
}