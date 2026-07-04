package com.hussain.data_api.service;

import com.hussain.data_api.model.DataRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.Query;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.ResultQuery;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class QueryExecutor {

    @SuppressWarnings({"unchecked", "rawtypes"})
    public Object execute(Query query, DataRequest.Operation operation) {
        log.debug("Executing query: {}", query.getSQL());

        try {
            return switch (operation) {
                case READ -> {
                    // For READ operations, we need to execute as a ResultQuery
                    if (query instanceof ResultQuery) {
                        ResultQuery<?> resultQuery = (ResultQuery<?>) query;
                        Result<Record> result = (Result<Record>) resultQuery.fetch();
                        yield result;
                    } else {
                        // Fallback: execute and return rows affected
                        int rows = query.execute();
                        yield rows;
                    }
                }
                case CREATE, BULK_CREATE -> {
                    int rows = query.execute();
                    yield rows;
                }
                case UPDATE, BULK_UPDATE -> {
                    int rows = query.execute();
                    yield rows;
                }
                case DELETE, BULK_DELETE -> {
                    int rows = query.execute();
                    yield rows;
                }
                default -> throw new IllegalArgumentException("Unsupported operation: " + operation);
            };
        } catch (Exception e) {
            log.error("Error executing query: {}", e.getMessage());
            throw new RuntimeException("Query execution failed: " + e.getMessage(), e);
        }
    }

    public long count(Query query) {
        log.debug("Counting query: {}", query.getSQL());
        try {
            // Execute the query and count the results
            if (query instanceof ResultQuery) {
                ResultQuery<?> resultQuery = (ResultQuery<?>) query;
                Result<?> result = resultQuery.fetch();
                return result.size();
            }
            return 0;
        } catch (Exception e) {
            log.error("Error counting query: {}", e.getMessage());
            return 0;
        }
    }
}