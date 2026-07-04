package com.hussain.data_api.service;

import com.hussain.data_api.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class DataValidator {

    public void validate(DataRequest request) {
        List<String> errors = new ArrayList<>();

        if (request.getOperation() == null) {
            errors.add("Operation is required");
        }

        if (!StringUtils.hasText(request.getEntity())) {
            errors.add("Entity name is required");
        }

        if (request.getOperation() == DataRequest.Operation.READ) {
            validateReadRequest(request, errors);
        } else if (request.getOperation() == DataRequest.Operation.CREATE ||
                request.getOperation() == DataRequest.Operation.UPDATE) {
            validateWriteRequest(request, errors);
        } else if (request.getOperation() == DataRequest.Operation.DELETE) {
            validateDeleteRequest(request, errors);
        } else if (request.getOperation() == DataRequest.Operation.BULK_CREATE ||
                request.getOperation() == DataRequest.Operation.BULK_UPDATE ||
                request.getOperation() == DataRequest.Operation.BULK_DELETE) {
            validateBulkRequest(request, errors);
        }

        if (!errors.isEmpty()) {
            throw new IllegalArgumentException("Validation failed: " + String.join(", ", errors));
        }
    }

    private void validateReadRequest(DataRequest request, List<String> errors) {
        if (request.getFilters() != null) {
            for (Filter filter : request.getFilters()) {
                if (!StringUtils.hasText(filter.getColumn())) {
                    errors.add("Filter column is required");
                }
                if (filter.getOperator() == null) {
                    errors.add("Filter operator is required for column: " + filter.getColumn());
                }
                if (filter.getValue() == null &&
                        filter.getOperator() != Filter.Operator.IS_NULL &&
                        filter.getOperator() != Filter.Operator.IS_NOT_NULL) {
                    errors.add("Filter value is required for column: " + filter.getColumn() +
                            " with operator: " + filter.getOperator());
                }
            }
        }

        if (request.getGroupBy() != null && !request.getGroupBy().isEmpty()) {
            validateGroupBy(request, errors);
        }

        if (request.getHaving() != null && !request.getHaving().isEmpty()) {
            validateHaving(request, errors);
        }

        if (request.getLimit() != null && request.getLimit() > 1000) {
            errors.add("Limit cannot exceed 1000");
        }
    }

    private void validateGroupBy(DataRequest request, List<String> errors) {
        for (GroupBy groupBy : request.getGroupBy()) {
            if (!StringUtils.hasText(groupBy.getColumn())) {
                errors.add("GroupBy column is required");
            }
            if (groupBy.getType() == GroupBy.GroupByType.DATE_TRUNC &&
                    !StringUtils.hasText(groupBy.getFormat())) {
                errors.add("Format is required for DATE_TRUNC group by type");
            }
        }
    }

    private void validateHaving(DataRequest request, List<String> errors) {
        for (Having having : request.getHaving()) {
            if (!StringUtils.hasText(having.getColumn())) {
                errors.add("Having column is required");
            }
            if (having.getAggregate() == null) {
                errors.add("Having aggregate function is required");
            }
            if (having.getOperator() == null) {
                errors.add("Having operator is required");
            }
            if (having.getValue() == null &&
                    having.getOperator() != Having.Operator.IS_NULL &&
                    having.getOperator() != Having.Operator.IS_NOT_NULL) {
                errors.add("Having value is required");
            }
            if (request.getGroupBy() == null || request.getGroupBy().isEmpty()) {
                errors.add("GROUP BY is required when using HAVING clause");
            }
        }
    }

    private void validateWriteRequest(DataRequest request, List<String> errors) {
        if (request.getData() == null || request.getData().isEmpty()) {
            errors.add("Data is required for write operations");
        }

        if (request.getGroupBy() != null && !request.getGroupBy().isEmpty()) {
            errors.add("GROUP BY is not supported for write operations");
        }
        if (request.getHaving() != null && !request.getHaving().isEmpty()) {
            errors.add("HAVING is not supported for write operations");
        }
    }

    private void validateDeleteRequest(DataRequest request, List<String> errors) {
        if ((request.getFilters() == null || request.getFilters().isEmpty()) &&
                (request.getData() == null || request.getData().isEmpty())) {
            errors.add("Filters or data with IDs is required for delete operations");
        }

        if (request.getGroupBy() != null && !request.getGroupBy().isEmpty()) {
            errors.add("GROUP BY is not supported for delete operations");
        }
        if (request.getHaving() != null && !request.getHaving().isEmpty()) {
            errors.add("HAVING is not supported for delete operations");
        }
    }

    private void validateBulkRequest(DataRequest request, List<String> errors) {
        if (request.getData() == null || request.getData().isEmpty()) {
            errors.add("Data is required for bulk operations");
        }

        if (!request.getData().containsKey("items") && !request.getData().containsKey("ids")) {
            errors.add("Bulk operations require 'items' or 'ids' in data");
        }

        if (request.getGroupBy() != null && !request.getGroupBy().isEmpty()) {
            errors.add("GROUP BY is not supported for bulk operations");
        }
        if (request.getHaving() != null && !request.getHaving().isEmpty()) {
            errors.add("HAVING is not supported for bulk operations");
        }
    }
}