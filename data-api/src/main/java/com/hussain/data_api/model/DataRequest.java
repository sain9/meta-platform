package com.hussain.data_api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DataRequest {
    private Operation operation;
    private String entity;
    private List<String> columns;
    private List<Filter> filters;
    private List<OrderBy> orderBy;
    private List<GroupBy> groupBy;
    private List<Having> having;
    private Integer limit;
    private Integer offset;
    private Map<String, Object> data; // For CREATE/UPDATE operations

    public enum Operation {
        READ, CREATE, UPDATE, DELETE, BULK_CREATE, BULK_UPDATE, BULK_DELETE
    }
}