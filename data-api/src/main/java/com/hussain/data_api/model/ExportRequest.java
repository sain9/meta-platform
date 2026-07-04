package com.hussain.data_api.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExportRequest {
    private String entity;
    private List<String> columns;
    private List<Filter> filters;
    private List<OrderBy> orderBy;
    private String format;
    private String fileName;
    private Integer limit;
}