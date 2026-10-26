package com.hussain.data_api_client.model;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class DataApiResponse {
    private boolean success;
    private String message;
    private List<Map<String, Object>> rows;   // for READ (structured row-maps)
    private Object data;                       // for CREATE/UPDATE/DELETE (e.g. {affectedRows: N})
    private Pagination pagination;
    private List<ValidationError> errors;

    @Data
    public static class Pagination {
        private int limit;
        private int offset;
        private long totalCount;
        private int totalPages;
    }

    @Data
    public static class ValidationError {
        private String field;
        private String message;
        private Object rejectedValue;
    }
}