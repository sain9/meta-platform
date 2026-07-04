package com.hussain.data_api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Having {
    private String column;
    private AggregateFunction aggregate;
    private Operator operator;
    private Object value;
    private LogicalOperator logicalOperator;
    
    public enum AggregateFunction {
        COUNT, SUM, AVG, MIN, MAX, COUNT_DISTINCT
    }
    
    public enum Operator {
        EQ, NEQ, GT, GTE, LT, LTE, LIKE, IN, NOT_IN, IS_NULL, IS_NOT_NULL,
        BETWEEN, CONTAINS, STARTS_WITH, ENDS_WITH
    }
    
    public enum LogicalOperator {
        AND, OR
    }
}