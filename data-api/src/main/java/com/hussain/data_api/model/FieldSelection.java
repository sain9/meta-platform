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
public class FieldSelection {
    private List<String> fields;
    private List<Aggregation> aggregations;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Aggregation {
        private AggregationType type;
        private String field;
        private String alias;
        
        public enum AggregationType {
            COUNT, SUM, AVG, MIN, MAX, COUNT_DISTINCT
        }
    }
}