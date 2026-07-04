package com.hussain.data_api.service;

import com.hussain.data_api.config.DataApiProperties;
import com.hussain.data_api.model.DataRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class QueryLoggingService {
    
    private final DataApiProperties properties;
    
    public void logQuery(DataRequest request) {
        if (!properties.isEnableQueryLogging()) {
            return;
        }
        
        log.info("Query: operation={}, entity={}, columns={}, filters={}, groupBy={}, having={}",
                request.getOperation(),
                request.getEntity(),
                request.getColumns(),
                request.getFilters(),
                request.getGroupBy(),
                request.getHaving());
    }
}