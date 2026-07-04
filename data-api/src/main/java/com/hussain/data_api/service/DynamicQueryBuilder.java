package com.hussain.data_api.service;

import com.hussain.data_api.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.jooq.Query;
import org.jooq.ResultQuery;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DynamicQueryBuilder {

    private final DSLContext dslContext;
    private final BatchQuery batchQuery;

    public Query buildQuery(DataRequest request) {
        return switch (request.getOperation()) {
            case READ -> buildSelectQuery(request);
            case CREATE -> buildInsertQuery(request);
            case UPDATE -> buildUpdateQuery(request);
            case DELETE -> buildDeleteQuery(request);
            case BULK_CREATE, BULK_UPDATE, BULK_DELETE ->
                    throw new UnsupportedOperationException(
                            "Bulk operations should be executed using executeBulkOperation() method"
                    );
            default -> throw new IllegalArgumentException("Unsupported operation: " + request.getOperation());
        };
    }

    public int executeBulkOperation(DataRequest request) {
        return switch (request.getOperation()) {
            case BULK_CREATE -> executeBulkInsert(request);
            case BULK_UPDATE -> executeBulkUpdate(request);
            case BULK_DELETE -> executeBulkDelete(request);
            default -> throw new IllegalArgumentException("Unsupported bulk operation: " + request.getOperation());
        };
    }

    private ResultQuery<?> buildSelectQuery(DataRequest request) {
        String entity = request.getEntity();

        // Build SELECT clause
        String selectClause = buildSelectClause(request);

        // Build FROM clause
        String fromClause = "FROM " + entity;

        // Build WHERE clause
        String whereClause = buildWhereClause(request.getFilters());

        // Build GROUP BY clause
        String groupByClause = buildGroupByClause(request.getGroupBy());

        // Build HAVING clause
        String havingClause = buildHavingClause(request.getHaving());

        // Build ORDER BY clause
        String orderByClause = buildOrderByClause(request.getOrderBy());

        // Build LIMIT and OFFSET
        String limitClause = buildLimitClause(request.getLimit(), request.getOffset());

        // Assemble the full SQL
        StringBuilder sql = new StringBuilder("SELECT ");
        sql.append(selectClause);
        sql.append(" ").append(fromClause);

        if (!whereClause.isEmpty()) {
            sql.append(" WHERE ").append(whereClause);
        }

        if (!groupByClause.isEmpty()) {
            sql.append(" GROUP BY ").append(groupByClause);
        }

        if (!havingClause.isEmpty()) {
            sql.append(" HAVING ").append(havingClause);
        }

        if (!orderByClause.isEmpty()) {
            sql.append(" ORDER BY ").append(orderByClause);
        }

        if (!limitClause.isEmpty()) {
            sql.append(" ").append(limitClause);
        }

        log.debug("Generated SQL: {}", sql.toString());

        // Return as ResultQuery for fetch operations
        return dslContext.resultQuery(sql.toString());
    }

    // Method to build a COUNT query for pagination
    public long getCount(DataRequest request) {
        String entity = request.getEntity();

        // Build COUNT query
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM " + entity);

        // Build WHERE clause
        String whereClause = buildWhereClause(request.getFilters());
        if (!whereClause.isEmpty()) {
            sql.append(" WHERE ").append(whereClause);
        }

        log.debug("Generated COUNT SQL: {}", sql.toString());

        // Execute and return count
        ResultQuery<?> query = dslContext.resultQuery(sql.toString());
        return (long) query.fetch().get(0).get(0);
    }

    private Query buildInsertQuery(DataRequest request) {
        String entity = request.getEntity();
        Map<String, Object> data = request.getData();

        if (data == null || data.isEmpty()) {
            throw new IllegalArgumentException("Data is required for INSERT operation");
        }

        // Build column names and values
        List<String> columns = new ArrayList<>();
        List<String> values = new ArrayList<>();

        for (Map.Entry<String, Object> entry : data.entrySet()) {
            columns.add(entry.getKey());
            Object value = entry.getValue();
            if (value instanceof String) {
                values.add("'" + value.toString().replace("'", "''") + "'");
            } else if (value == null) {
                values.add("NULL");
            } else {
                values.add(value.toString());
            }
        }

        String sql = "INSERT INTO " + entity +
                " (" + String.join(", ", columns) + ")" +
                " VALUES (" + String.join(", ", values) + ")";

        log.debug("Generated SQL: {}", sql);
        return dslContext.query(sql);
    }

    private Query buildUpdateQuery(DataRequest request) {
        String entity = request.getEntity();
        Map<String, Object> data = request.getData();

        if (data == null || data.isEmpty()) {
            throw new IllegalArgumentException("Data is required for UPDATE operation");
        }

        // Build SET clause
        List<String> setClauses = new ArrayList<>();
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            String column = entry.getKey();
            Object value = entry.getValue();
            String valueStr;
            if (value instanceof String) {
                valueStr = "'" + value.toString().replace("'", "''") + "'";
            } else if (value == null) {
                valueStr = "NULL";
            } else {
                valueStr = value.toString();
            }
            setClauses.add(column + " = " + valueStr);
        }

        // Build WHERE clause
        String whereClause = buildWhereClause(request.getFilters());

        String sql = "UPDATE " + entity +
                " SET " + String.join(", ", setClauses);

        if (!whereClause.isEmpty()) {
            sql += " WHERE " + whereClause;
        }

        log.debug("Generated SQL: {}", sql);
        return dslContext.query(sql);
    }

    private Query buildDeleteQuery(DataRequest request) {
        String entity = request.getEntity();
        String whereClause = buildWhereClause(request.getFilters());

        String sql = "DELETE FROM " + entity;
        if (!whereClause.isEmpty()) {
            sql += " WHERE " + whereClause;
        }

        log.debug("Generated SQL: {}", sql);
        return dslContext.query(sql);
    }

    private int executeBulkInsert(DataRequest request) {
        String entity = request.getEntity();
        List<Map<String, Object>> dataList = (List<Map<String, Object>>) request.getData().get("items");

        if (dataList == null || dataList.isEmpty()) {
            return 0;
        }

        List<Query> queries = new ArrayList<>();
        for (Map<String, Object> data : dataList) {
            List<String> columns = new ArrayList<>();
            List<String> values = new ArrayList<>();

            for (Map.Entry<String, Object> entry : data.entrySet()) {
                columns.add(entry.getKey());
                Object value = entry.getValue();
                if (value instanceof String) {
                    values.add("'" + value.toString().replace("'", "''") + "'");
                } else if (value == null) {
                    values.add("NULL");
                } else {
                    values.add(value.toString());
                }
            }

            String sql = "INSERT INTO " + entity +
                    " (" + String.join(", ", columns) + ")" +
                    " VALUES (" + String.join(", ", values) + ")";

            queries.add(dslContext.query(sql));
        }

        return batchQuery.executeBatch(queries);
    }

    private int executeBulkUpdate(DataRequest request) {
        String entity = request.getEntity();
        List<Map<String, Object>> dataList = (List<Map<String, Object>>) request.getData().get("items");

        if (dataList == null || dataList.isEmpty()) {
            return 0;
        }

        List<Query> queries = new ArrayList<>();
        for (Map<String, Object> item : dataList) {
            Map<String, Object> updateData = new HashMap<>(item);
            Object id = updateData.remove("id");

            if (id == null) {
                throw new IllegalArgumentException("'id' is required for bulk update");
            }

            List<String> setClauses = new ArrayList<>();
            for (Map.Entry<String, Object> entry : updateData.entrySet()) {
                String column = entry.getKey();
                Object value = entry.getValue();
                String valueStr;
                if (value instanceof String) {
                    valueStr = "'" + value.toString().replace("'", "''") + "'";
                } else if (value == null) {
                    valueStr = "NULL";
                } else {
                    valueStr = value.toString();
                }
                setClauses.add(column + " = " + valueStr);
            }

            String sql = "UPDATE " + entity +
                    " SET " + String.join(", ", setClauses) +
                    " WHERE id = " + id;

            queries.add(dslContext.query(sql));
        }

        return batchQuery.executeBatch(queries);
    }

    private int executeBulkDelete(DataRequest request) {
        String entity = request.getEntity();
        List<Object> ids = (List<Object>) request.getData().get("ids");

        if (ids == null || ids.isEmpty()) {
            return 0;
        }

        String idList = ids.stream()
                .map(id -> id.toString())
                .collect(Collectors.joining(", "));

        String sql = "DELETE FROM " + entity + " WHERE id IN (" + idList + ")";
        return dslContext.query(sql).execute();
    }

    private String buildSelectClause(DataRequest request) {
        List<String> columns = request.getColumns();

        if (columns == null || columns.isEmpty()) {
            boolean hasGroupBy = request.getGroupBy() != null && !request.getGroupBy().isEmpty();
            if (hasGroupBy) {
                // For GROUP BY, we need to select group by columns and an aggregate
                List<String> groupColumns = new ArrayList<>();
                for (GroupBy groupBy : request.getGroupBy()) {
                    String columnName = groupBy.getAlias() != null ?
                            groupBy.getAlias() : groupBy.getColumn();
                    groupColumns.add(columnName);
                }
                groupColumns.add("COUNT(*) as total_count");
                return String.join(", ", groupColumns);
            }
            return "*";
        }

        return String.join(", ", columns);
    }

    private String buildWhereClause(List<Filter> filters) {
        if (filters == null || filters.isEmpty()) {
            return "";
        }

        List<String> conditions = new ArrayList<>();
        for (Filter filter : filters) {
            String condition = buildCondition(filter);
            conditions.add(condition);
        }

        // Join with AND/OR based on logical operators
        StringBuilder where = new StringBuilder();
        for (int i = 0; i < conditions.size(); i++) {
            if (i > 0) {
                Filter filter = filters.get(i);
                if (filter.getLogicalOperator() == Filter.LogicalOperator.OR) {
                    where.append(" OR ");
                } else {
                    where.append(" AND ");
                }
            }
            where.append(conditions.get(i));
        }

        return where.toString();
    }

    private String buildCondition(Filter filter) {
        String column = filter.getColumn();
        Object value = filter.getValue();
        String valueStr;

        if (value instanceof String) {
            valueStr = "'" + value.toString().replace("'", "''") + "'";
        } else if (value == null) {
            valueStr = "NULL";
        } else {
            valueStr = value.toString();
        }

        return switch (filter.getOperator()) {
            case EQ -> column + " = " + valueStr;
            case NEQ -> column + " <> " + valueStr;
            case GT -> column + " > " + valueStr;
            case GTE -> column + " >= " + valueStr;
            case LT -> column + " < " + valueStr;
            case LTE -> column + " <= " + valueStr;
            case LIKE -> column + " LIKE '%" + value + "%'";
            case ILIKE -> column + " ILIKE '%" + value + "%'";
            case IN -> {
                List<?> values = (List<?>) value;
                String inValues = values.stream()
                        .map(v -> v instanceof String ? "'" + v.toString().replace("'", "''") + "'" : v.toString())
                        .collect(Collectors.joining(", "));
                yield column + " IN (" + inValues + ")";
            }
            case NOT_IN -> {
                List<?> values = (List<?>) value;
                String inValues = values.stream()
                        .map(v -> v instanceof String ? "'" + v.toString().replace("'", "''") + "'" : v.toString())
                        .collect(Collectors.joining(", "));
                yield column + " NOT IN (" + inValues + ")";
            }
            case IS_NULL -> column + " IS NULL";
            case IS_NOT_NULL -> column + " IS NOT NULL";
            case BETWEEN -> {
                List<?> values = (List<?>) value;
                String v1 = values.get(0) instanceof String ? "'" + values.get(0).toString().replace("'", "''") + "'" : values.get(0).toString();
                String v2 = values.get(1) instanceof String ? "'" + values.get(1).toString().replace("'", "''") + "'" : values.get(1).toString();
                yield column + " BETWEEN " + v1 + " AND " + v2;
            }
            case CONTAINS -> column + " LIKE '%" + value + "%'";
            case STARTS_WITH -> column + " LIKE '" + value + "%'";
            case ENDS_WITH -> column + " LIKE '%" + value + "'";
        };
    }

    private String buildGroupByClause(List<GroupBy> groupByList) {
        if (groupByList == null || groupByList.isEmpty()) {
            return "";
        }

        List<String> groupColumns = new ArrayList<>();
        for (GroupBy groupBy : groupByList) {
            String column = groupBy.getColumn();
            String type = groupBy.getType() != null ? groupBy.getType().toString() : "NORMAL";
            String format = groupBy.getFormat();
            String alias = groupBy.getAlias();

            String expression = switch (type) {
                case "YEAR" -> "EXTRACT(YEAR FROM " + column + ")";
                case "MONTH" -> "EXTRACT(MONTH FROM " + column + ")";
                case "DAY" -> "EXTRACT(DAY FROM " + column + ")";
                case "HOUR" -> "EXTRACT(HOUR FROM " + column + ")";
                case "WEEK" -> "EXTRACT(WEEK FROM " + column + ")";
                case "QUARTER" -> "EXTRACT(QUARTER FROM " + column + ")";
                case "DATE_TRUNC" -> {
                    String fmt = format != null ? format : "day";
                    yield switch (fmt.toLowerCase()) {
                        case "year" -> "DATE_TRUNC('year', " + column + ")";
                        case "month" -> "DATE_TRUNC('month', " + column + ")";
                        case "day" -> "DATE_TRUNC('day', " + column + ")";
                        case "hour" -> "DATE_TRUNC('hour', " + column + ")";
                        default -> column;
                    };
                }
                default -> column;
            };

            if (alias != null && !alias.isEmpty()) {
                groupColumns.add(expression + " AS " + alias);
            } else {
                groupColumns.add(expression);
            }
        }

        return String.join(", ", groupColumns);
    }

    private String buildHavingClause(List<Having> havingList) {
        if (havingList == null || havingList.isEmpty()) {
            return "";
        }

        List<String> havingConditions = new ArrayList<>();
        for (Having having : havingList) {
            String aggregate = having.getAggregate().toString();
            String column = having.getColumn();
            String aggregateExpr = aggregate + "(" + column + ")";

            String condition = switch (having.getOperator()) {
                case EQ -> aggregateExpr + " = " + having.getValue();
                case NEQ -> aggregateExpr + " <> " + having.getValue();
                case GT -> aggregateExpr + " > " + having.getValue();
                case GTE -> aggregateExpr + " >= " + having.getValue();
                case LT -> aggregateExpr + " < " + having.getValue();
                case LTE -> aggregateExpr + " <= " + having.getValue();
                case LIKE -> aggregateExpr + " LIKE '%" + having.getValue() + "%'";
                case IN -> {
                    List<?> values = (List<?>) having.getValue();
                    String inValues = values.stream()
                            .map(v -> v instanceof String ? "'" + v.toString().replace("'", "''") + "'" : v.toString())
                            .collect(Collectors.joining(", "));
                    yield aggregateExpr + " IN (" + inValues + ")";
                }
                case NOT_IN -> {
                    List<?> values = (List<?>) having.getValue();
                    String inValues = values.stream()
                            .map(v -> v instanceof String ? "'" + v.toString().replace("'", "''") + "'" : v.toString())
                            .collect(Collectors.joining(", "));
                    yield aggregateExpr + " NOT IN (" + inValues + ")";
                }
                case IS_NULL -> aggregateExpr + " IS NULL";
                case IS_NOT_NULL -> aggregateExpr + " IS NOT NULL";
                case BETWEEN -> {
                    List<?> values = (List<?>) having.getValue();
                    yield aggregateExpr + " BETWEEN " + values.get(0) + " AND " + values.get(1);
                }
                case CONTAINS -> aggregateExpr + " LIKE '%" + having.getValue() + "%'";
                case STARTS_WITH -> aggregateExpr + " LIKE '" + having.getValue() + "%'";
                case ENDS_WITH -> aggregateExpr + " LIKE '%" + having.getValue() + "'";
            };

            havingConditions.add(condition);
        }

        return String.join(" AND ", havingConditions);
    }

    private String buildOrderByClause(List<OrderBy> orderByList) {
        if (orderByList == null || orderByList.isEmpty()) {
            return "";
        }

        List<String> orderClauses = new ArrayList<>();
        for (OrderBy orderBy : orderByList) {
            String direction = orderBy.getDirection() == OrderBy.Direction.DESC ? "DESC" : "ASC";
            orderClauses.add(orderBy.getColumn() + " " + direction);
        }

        return String.join(", ", orderClauses);
    }

    private String buildLimitClause(Integer limit, Integer offset) {
        if (limit == null || limit <= 0) {
            return "";
        }

        if (offset != null && offset >= 0) {
            return "LIMIT " + limit + " OFFSET " + offset;
        }
        return "LIMIT " + limit;
    }
}