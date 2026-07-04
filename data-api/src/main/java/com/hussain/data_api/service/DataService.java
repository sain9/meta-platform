package com.hussain.data_api.service;

import com.hussain.data_api.exception.DataApiException;
import com.hussain.data_api.model.DataRequest;
import com.hussain.data_api.model.DataResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.Record;
import org.jooq.Result;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DataService {

    private final DynamicQueryBuilder queryBuilder;
    private final DataValidator dataValidator;
    private final QueryExecutor queryExecutor;
    private final AuditService auditService;
    private final QueryLoggingService queryLoggingService;

    @Transactional
    public DataResponse<Object> processRequest(DataRequest request) {
        String transactionId = UUID.randomUUID().toString();
        long startTime = System.currentTimeMillis();

        try {
            log.info("Processing request: transactionId={}, operation={}, entity={}",
                    transactionId, request.getOperation(), request.getEntity());

            // Validate the request
            dataValidator.validate(request);

            // Log the query (if enabled)
            queryLoggingService.logQuery(request);

            Object result;

            // Handle bulk operations separately
            if (isBulkOperation(request.getOperation())) {
                int rowsAffected = queryBuilder.executeBulkOperation(request);
                result = Map.of("affectedRows", rowsAffected);
            } else {
                // Build and execute regular query
                var query = queryBuilder.buildQuery(request);
                Object rawResult = queryExecutor.execute(query, request.getOperation());
                // Transform the result to a serializable format
                result = transformToSerializable(rawResult);
            }

            // Audit the operation
            auditService.audit(request, result, transactionId, startTime, true, null);

            // Build response
            DataResponse<Object> response = DataResponse.builder()
                    .success(true)
                    .message("Operation completed successfully")
                    .data(result)
                    .build();

            // Add pagination for READ operations
            if (request.getOperation() == DataRequest.Operation.READ) {
                addPagination(response, request);
            }

            log.info("Request completed successfully: transactionId={}, duration={}ms",
                    transactionId, System.currentTimeMillis() - startTime);

            return response;

        } catch (Exception e) {
            log.error("Error processing request: transactionId={}, error={}",
                    transactionId, e.getMessage(), e);

            auditService.audit(request, null, transactionId, startTime, false, e.getMessage());

            throw new DataApiException("Operation failed: " + e.getMessage(), "DATA_API_ERROR", 500);
        }
    }

    @SuppressWarnings("unchecked")
    private Object transformToSerializable(Object rawResult) {
        if (rawResult instanceof Result) {
            Result<?> result = (Result<?>) rawResult;
            return result.stream()
                    .map(this::recordToMap)
                    .collect(Collectors.toList());
        }

        if (rawResult instanceof Record) {
            return recordToMap((Record) rawResult);
        }

        if (rawResult instanceof Number) {
            return Map.of("affectedRows", rawResult);
        }

        if (rawResult instanceof List) {
            List<?> list = (List<?>) rawResult;
            if (!list.isEmpty() && list.get(0) instanceof Record) {
                return list.stream()
                        .map(item -> recordToMap((Record) item))
                        .collect(Collectors.toList());
            }
            return rawResult;
        }

        if (rawResult instanceof Map) {
            return rawResult;
        }

        return rawResult;
    }

    private Map<String, Object> recordToMap(Record record) {
        Map<String, Object> map = new HashMap<>();
        for (org.jooq.Field<?> field : record.fields()) {
            map.put(field.getName(), record.get(field));
        }
        return map;
    }

    private boolean isBulkOperation(DataRequest.Operation operation) {
        return operation == DataRequest.Operation.BULK_CREATE ||
                operation == DataRequest.Operation.BULK_UPDATE ||
                operation == DataRequest.Operation.BULK_DELETE;
    }

    @Transactional
    public DataResponse<Object> batchProcess(List<DataRequest> requests) {
        String transactionId = UUID.randomUUID().toString();
        long startTime = System.currentTimeMillis();

        try {
            log.info("Processing batch request: transactionId={}, size={}",
                    transactionId, requests.size());

            List<Object> results = requests.stream()
                    .map(request -> {
                        try {
                            if (isBulkOperation(request.getOperation())) {
                                int rowsAffected = queryBuilder.executeBulkOperation(request);
                                return Map.of("affectedRows", rowsAffected);
                            } else {
                                var query = queryBuilder.buildQuery(request);
                                Object rawResult = queryExecutor.execute(query, request.getOperation());
                                return transformToSerializable(rawResult);
                            }
                        } catch (Exception e) {
                            log.error("Error in batch item: {}", e.getMessage());
                            throw new DataApiException("Batch item failed: " + e.getMessage());
                        }
                    })
                    .collect(Collectors.toList());

            log.info("Batch request completed: transactionId={}, duration={}ms",
                    transactionId, System.currentTimeMillis() - startTime);

            return DataResponse.builder()
                    .success(true)
                    .message("Batch operation completed successfully")
                    .data(results)
                    .build();

        } catch (Exception e) {
            log.error("Batch request failed: transactionId={}, error={}",
                    transactionId, e.getMessage(), e);

            throw new DataApiException("Batch operation failed: " + e.getMessage());
        }
    }

    private void addPagination(DataResponse<Object> response, DataRequest request) {
        if (request.getLimit() != null && request.getLimit() > 0) {
            // Get total count from the query builder
            long totalCount = queryBuilder.getCount(request);

            DataResponse.Pagination pagination = DataResponse.Pagination.builder()
                    .limit(request.getLimit())
                    .offset(request.getOffset() != null ? request.getOffset() : 0)
                    .totalCount(totalCount)
                    .totalPages((int) Math.ceil((double) totalCount / request.getLimit()))
                    .build();
            response.setPagination(pagination);
        }
    }
}