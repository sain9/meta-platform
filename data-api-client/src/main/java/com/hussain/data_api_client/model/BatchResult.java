package com.hussain.data_api_client.model;

import lombok.Data;
import java.util.List;

@Data
public class BatchResult {
    private boolean success;
    private String message;
    private List<DataApiResponse> results;
}