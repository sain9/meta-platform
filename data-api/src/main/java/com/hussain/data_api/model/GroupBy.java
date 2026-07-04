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
public class GroupBy {
    private String column;
    private String alias; // Optional alias for the grouped column
    private GroupByType type; // For complex groupings like date truncation, etc.
    private String format; // For date/time formatting
    
    public enum GroupByType {
        NORMAL,
        YEAR,
        MONTH,
        DAY,
        HOUR,
        WEEK,
        QUARTER,
        DATE_TRUNC
    }
}