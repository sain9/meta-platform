package com.hussain.data_api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FilterGroup {
    private LogicalOperator logicalOperator;
    private List<Filter> filters;
    private List<FilterGroup> groups;
    
    public enum LogicalOperator {
        AND, OR
    }
}