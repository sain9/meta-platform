package com.hussain.data_api.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.jooq.Query;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class BatchQuery {

    private final DSLContext dslContext;

    public int executeBatch(List<Query> queries) {
        if (queries == null || queries.isEmpty()) {
            return 0;
        }

        log.debug("Executing batch of {} queries", queries.size());
        var batch = dslContext.batch(queries);
        int[] results = batch.execute();

        // Sum up all affected rows
        int totalRows = 0;
        for (int rows : results) {
            totalRows += rows;
        }
        return totalRows;
    }

    public int executeTransactional(List<Query> queries) {
        return dslContext.transactionResult(configuration -> {
            DSLContext ctx = DSL.using(configuration);
            var batch = ctx.batch(queries);
            int[] results = batch.execute();

            int totalRows = 0;
            for (int rows : results) {
                totalRows += rows;
            }
            return totalRows;
        });
    }

    public List<QueryResult> executeWithResults(List<Query> queries) {
        if (queries == null || queries.isEmpty()) {
            return List.of();
        }

        return queries.stream()
                .map(query -> {
                    try {
                        int result = query.execute();
                        return new QueryResult(query.getSQL(), result, null);
                    } catch (Exception e) {
                        log.error("Error executing query: {}", query.getSQL(), e);
                        return new QueryResult(query.getSQL(), 0, e.getMessage());
                    }
                })
                .toList();
    }

    public int executeWithRollback(List<Query> queries) {
        return dslContext.transactionResult(configuration -> {
            DSLContext ctx = DSL.using(configuration);
            int totalRows = 0;

            for (int i = 0; i < queries.size(); i++) {
                Query query = queries.get(i);
                try {
                    int rows = query.execute();
                    totalRows += rows;
                } catch (Exception e) {
                    log.error("Error in batch execution at index {}: {}", i, e.getMessage());
                    throw new RuntimeException("Batch execution failed: " + e.getMessage(), e);
                }
            }

            return totalRows;
        });
    }

    public record QueryResult(String sql, int affectedRows, String error) {}
}