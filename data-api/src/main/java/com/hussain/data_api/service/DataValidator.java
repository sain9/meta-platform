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

    private static final int MAX_DEPTH = 5;

    public void validate(DataRequest request) {
        List<String> errors = new ArrayList<>();
        validateRequest(request, errors, 0);
        if (!errors.isEmpty()) {
            throw new IllegalArgumentException("Validation failed: " + String.join(", ", errors));
        }
    }

    private void validateRequest(DataRequest request, List<String> errors, int depth) {
        if (depth > MAX_DEPTH) {
            errors.add("Subquery nesting depth exceeds maximum of " + MAX_DEPTH);
            return;
        }
        if (request.getOperation() == null) {
            errors.add("Operation is required");
            return;
        }

        boolean hasEntity = StringUtils.hasText(request.getEntity());
        boolean hasFrom   = request.getFrom() != null;
        if (!hasEntity && !hasFrom) {
            errors.add("Entity or 'from' clause is required");
        }

        if (hasFrom) {
            FromClause from = request.getFrom();
            if (from.getEntity() == null && from.getSubquery() == null) {
                errors.add("'from' must declare either 'entity' or 'subquery'");
            }
            if (from.getSubquery() != null) {
                if (!StringUtils.hasText(from.getAlias())) {
                    errors.add("Alias is required for subquery in FROM clause");
                }
                validateRequest(from.getSubquery().getQuery(), errors, depth + 1);
            }
        }

        if (request.getJoins() != null) {
            for (JoinClause join : request.getJoins()) {
                if (join.getType() == null) {
                    errors.add("Join type is required");
                }
                if (join.getEntity() == null && join.getSubquery() == null) {
                    errors.add("Join must declare either 'entity' or 'subquery'");
                }
                if (join.getSubquery() != null) {
                    if (!StringUtils.hasText(join.getAlias())) {
                        errors.add("Alias is required for joined subquery");
                    }
                    validateRequest(join.getSubquery().getQuery(), errors, depth + 1);
                }
                if (join.getType() != JoinClause.JoinType.CROSS
                        && (join.getOn() == null || join.getOn().isEmpty())) {
                    errors.add("ON clause is required for non-CROSS joins");
                }
                if (join.getOn() != null) {
                    for (JoinCondition c : join.getOn()) {
                        if (!StringUtils.hasText(c.getLeft())) {
                            errors.add("Join condition 'left' is required");
                        }
                        if (c.getOperator() == null) {
                            errors.add("Join condition operator is required");
                        }
                        if (c.getRight() == null
                                && c.getValue() == null
                                && c.getOperator() != Filter.Operator.IS_NULL
                                && c.getOperator() != Filter.Operator.IS_NOT_NULL) {
                            errors.add("Join condition requires 'right' or 'value'");
                        }
                    }
                }
            }
        }

        if (request.getFilterGroup() != null) {
            validateFilterGroup(request.getFilterGroup(), errors, depth);
        }

        if (request.getOperation() == DataRequest.Operation.READ) {
            validateReadRequest(request, errors);
        } else if (request.getOperation() == DataRequest.Operation.CREATE
                || request.getOperation() == DataRequest.Operation.UPDATE) {
            validateWriteRequest(request, errors);
        } else if (request.getOperation() == DataRequest.Operation.DELETE) {
            validateDeleteRequest(request, errors);
        } else if (request.getOperation() == DataRequest.Operation.BULK_CREATE
                || request.getOperation() == DataRequest.Operation.BULK_UPDATE
                || request.getOperation() == DataRequest.Operation.BULK_DELETE) {
            validateBulkRequest(request, errors);
        }
    }

    private void validateFilterGroup(FilterGroup group, List<String> errors, int depth) {
        if (depth > MAX_DEPTH) {
            errors.add("Filter group nesting too deep");
            return;
        }
        if (group.getFilters() != null) {
            for (Filter f : group.getFilters()) {
                if (!StringUtils.hasText(f.getColumn()) && f.getSubquery() == null) {
                    errors.add("Filter column is required");
                }
                if (f.getOperator() == null) {
                    errors.add("Filter operator is required");
                }
                if (f.getSubquery() != null) {
                    validateRequest(f.getSubquery().getQuery(), errors, depth + 1);
                } else if (f.getValue() == null
                        && f.getRightColumn() == null
                        && f.getOperator() != Filter.Operator.IS_NULL
                        && f.getOperator() != Filter.Operator.IS_NOT_NULL
                        && f.getOperator() != Filter.Operator.EXISTS
                        && f.getOperator() != Filter.Operator.NOT_EXISTS) {
                    errors.add("Filter value is required for column: " + f.getColumn()
                            + " operator: " + f.getOperator());
                }
            }
        }
        if (group.getGroups() != null) {
            for (FilterGroup g : group.getGroups()) {
                validateFilterGroup(g, errors, depth + 1);
            }
        }
    }

    private void validateReadRequest(DataRequest request, List<String> errors) {
        if (request.getFilters() != null) {
            for (Filter filter : request.getFilters()) {
                if (!StringUtils.hasText(filter.getColumn()) && filter.getSubquery() == null) {
                    errors.add("Filter column is required");
                }
                if (filter.getOperator() == null) {
                    errors.add("Filter operator is required for column: " + filter.getColumn());
                }
                if (filter.getValue() == null
                        && filter.getSubquery() == null
                        && filter.getRightColumn() == null
                        && filter.getOperator() != Filter.Operator.IS_NULL
                        && filter.getOperator() != Filter.Operator.IS_NOT_NULL
                        && filter.getOperator() != Filter.Operator.EXISTS
                        && filter.getOperator() != Filter.Operator.NOT_EXISTS) {
                    errors.add("Filter value is required for column: " + filter.getColumn()
                            + " with operator: " + filter.getOperator());
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
            if (groupBy.getType() == GroupBy.GroupByType.DATE_TRUNC
                    && !StringUtils.hasText(groupBy.getFormat())) {
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
            if (having.getValue() == null
                    && having.getOperator() != Having.Operator.IS_NULL
                    && having.getOperator() != Having.Operator.IS_NOT_NULL) {
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
        if ((request.getFilters() == null || request.getFilters().isEmpty())
                && (request.getData() == null || request.getData().isEmpty())
                && request.getFilterGroup() == null) {
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