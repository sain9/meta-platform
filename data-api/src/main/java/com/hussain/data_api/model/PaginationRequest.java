package com.hussain.data_api.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaginationRequest {
    @Builder.Default
    private int page = 1;
    
    @Builder.Default
    private int size = 100;
    
    @Builder.Default
    private String sortBy = "id";
    
    @Builder.Default
    private SortDirection direction = SortDirection.ASC;
    
    public enum SortDirection {
        ASC, DESC
    }
    
    public int getOffset() {
        return (page - 1) * size;
    }
}