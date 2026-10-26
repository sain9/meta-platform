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

    // =================================================================
    // SELECT
    // =================================================================
    private ResultQuery<?> buildSelectQuery(DataRequest request) {
        String selectClause  = buildSelectClause(request);
        String fromClause    = buildFromClause(request);
        String whereClause   = buildWhereClause(request);
        String groupByClause = buildGroupByClause(request.getGroupBy());
        String havingClause  = buildHavingClause(request.getHaving());
        String orderByClause = buildOrderByClause(request.getOrderBy());
        String limitClause   = buildLimitClause(request.getLimit(), request.getOffset());

        StringBuilder sql = new StringBuilder("SELECT ");
        sql.append(selectClause);
        sql.append(" ").append(fromClause);

        if (!whereClause.isEmpty())   sql.append(" WHERE ").append(whereClause);
        if (!groupByClause.isEmpty()) sql.append(" GROUP BY ").append(groupByClause);
        if (!havingClause.isEmpty())  sql.append(" HAVING ").append(havingClause);
        if (!orderByClause.isEmpty()) sql.append(" ORDER BY ").append(orderByClause);
        if (!limitClause.isEmpty())   sql.append(" ").append(limitClause);

        log.debug("Generated SQL: {}", sql);
        return dslContext.resultQuery(sql.toString());
    }

    public long getCount(DataRequest request) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) ");
        sql.append(buildFromClause(request));

        String whereClause = buildWhereClause(request);
        if (!whereClause.isEmpty()) {
            sql.append(" WHERE ").append(whereClause);
        }

        log.debug("Generated COUNT SQL: {}", sql);
        ResultQuery<?> query = dslContext.resultQuery(sql.toString());
        return (long) query.fetch().get(0).get(0);
    }

    // =================================================================
    // INSERT
    // =================================================================
    private Query buildInsertQuery(DataRequest request) {
        String entity = request.getEntity();
        Map<String, Object> data = request.getData();

        if (data == null || data.isEmpty()) {
            throw new IllegalArgumentException("Data is required for INSERT operation");
        }

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

    // =================================================================
    // UPDATE
    // =================================================================
    private Query buildUpdateQuery(DataRequest request) {
        String entity = request.getEntity();
        Map<String, Object> data = request.getData();

        if (data == null || data.isEmpty()) {
            throw new IllegalArgumentException("Data is required for UPDATE operation");
        }

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

        String whereClause = buildWhereClause(request);
        String sql = "UPDATE " + entity + " SET " + String.join(", ", setClauses);
        if (!whereClause.isEmpty()) {
            sql += " WHERE " + whereClause;
        }

        log.debug("Generated SQL: {}", sql);
        return dslContext.query(sql);
    }

    // =================================================================
    // DELETE
    // =================================================================
    private Query buildDeleteQuery(DataRequest request) {
        String entity = request.getEntity();
        String whereClause = buildWhereClause(request);

        String sql = "DELETE FROM " + entity;
        if (!whereClause.isEmpty()) {
            sql += " WHERE " + whereClause;
        }

        log.debug("Generated SQL: {}", sql);
        return dslContext.query(sql);
    }

    // =================================================================
    // BULK
    // =================================================================
    private int executeBulkInsert(DataRequest request) {
        String entity = request.getEntity();
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> dataList = (List<Map<String, Object>>) request.getData().get("items");

        if (dataList == null || dataList.isEmpty()) return 0;

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

        // Which column identifies the row to update? Caller may set bulkKeyColumn
        // on the request (e.g. "uuid"); otherwise fall back to legacy behaviour "id".
        String keyColumn = request.getBulkKeyColumn() != null && !request.getBulkKeyColumn().isBlank()
                ? request.getBulkKeyColumn()
                : "id";

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> dataList = (List<Map<String, Object>>) request.getData().get("items");

        if (dataList == null || dataList.isEmpty()) return 0;

        List<Query> queries = new ArrayList<>();
        for (Map<String, Object> item : dataList) {
            Map<String, Object> updateData = new HashMap<>(item);
            Object keyValue = updateData.remove(keyColumn);
            if (keyValue == null) {
                throw new IllegalArgumentException(
                        "'" + keyColumn + "' is required for bulk update, else provide bulkKeyColumn, e.g: \n \"bulkKeyColumn\": \"uuid\",\n" +
                                " \"data\": {");
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
                    " WHERE " + keyColumn + " = " + literal(keyValue);
            queries.add(dslContext.query(sql));
        }
        return batchQuery.executeBatch(queries);
    }

    private int executeBulkDelete(DataRequest request) {
        String entity = request.getEntity();
        @SuppressWarnings("unchecked")
        List<Object> ids = (List<Object>) request.getData().get("ids");

        if (ids == null || ids.isEmpty()) return 0;

        String idList = ids.stream()
                .map(Object::toString)
                .collect(Collectors.joining(", "));

        String sql = "DELETE FROM " + entity + " WHERE id IN (" + idList + ")";
        return dslContext.query(sql).execute();
    }

    // =================================================================
    // SELECT CLAUSE
    // =================================================================
    private String buildSelectClause(DataRequest request) {
        List<String> columns = request.getColumns();

        if (columns == null || columns.isEmpty()) {
            boolean hasGroupBy = request.getGroupBy() != null && !request.getGroupBy().isEmpty();
            if (hasGroupBy) {
                List<String> groupColumns = new ArrayList<>();
                for (GroupBy groupBy : request.getGroupBy()) {
                    String columnName = groupBy.getAlias() != null
                            ? groupBy.getAlias() : groupBy.getColumn();
                    groupColumns.add(columnName);
                }
                groupColumns.add("COUNT(*) as total_count");
                return String.join(", ", groupColumns);
            }
            return "*";
        }
        return String.join(", ", columns);
    }

    // =================================================================
    // WHERE DISPATCHER
    // =================================================================
    private String buildWhereClause(DataRequest request) {
        if (request.getFilterGroup() != null) {
            String sql = buildFilterGroupSql(request.getFilterGroup());
            return sql == null ? "" : sql;
        }
        return buildFlatWhereClause(request.getFilters());
    }

    private String buildFilterGroupSql(FilterGroup group) {
        if (group == null) return "";

        List<String> parts = new ArrayList<>();

        if (group.getFilters() != null) {
            for (Filter f : group.getFilters()) {
                String c = buildCondition(f);
                if (c != null && !c.isEmpty()) parts.add(c);
            }
        }
        if (group.getGroups() != null) {
            for (FilterGroup g : group.getGroups()) {
                String nested = buildFilterGroupSql(g);
                if (nested != null && !nested.isEmpty()) parts.add("(" + nested + ")");
            }
        }
        if (parts.isEmpty()) return "";

        String joiner = (group.getLogicalOperator() == FilterGroup.LogicalOperator.OR)
                ? " OR " : " AND ";
        return String.join(joiner, parts);
    }

    // =================================================================
    // FLAT WHERE (legacy — byte-identical to before)
    // =================================================================
    private String buildFlatWhereClause(List<Filter> filters) {
        if (filters == null || filters.isEmpty()) return "";

        List<String> conditions = new ArrayList<>();
        for (Filter filter : filters) {
            conditions.add(buildCondition(filter));
        }

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

    // =================================================================
    // SINGLE CONDITION
    // =================================================================
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

        // column-to-column comparison (used in JOIN ON / correlated WHERE)
        if (filter.getRightColumn() != null) {
            return column + " " + operatorSymbol(filter.getOperator()) + " " + filter.getRightColumn();
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
                if (filter.getSubquery() != null) {
                    yield column + " IN (" + renderSubquerySql(filter.getSubquery().getQuery()) + ")";
                }
                List<?> values = (List<?>) value;
                String inValues = values.stream()
                        .map(v -> v instanceof String ? "'" + v.toString().replace("'", "''") + "'" : v.toString())
                        .collect(Collectors.joining(", "));
                yield column + " IN (" + inValues + ")";
            }
            case NOT_IN -> {
                if (filter.getSubquery() != null) {
                    yield column + " NOT IN (" + renderSubquerySql(filter.getSubquery().getQuery()) + ")";
                }
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
                String v1 = values.get(0) instanceof String
                        ? "'" + values.get(0).toString().replace("'", "''") + "'" : values.get(0).toString();
                String v2 = values.get(1) instanceof String
                        ? "'" + values.get(1).toString().replace("'", "''") + "'" : values.get(1).toString();
                yield column + " BETWEEN " + v1 + " AND " + v2;
            }
            case CONTAINS -> column + " LIKE '%" + value + "%'";
            case STARTS_WITH -> column + " LIKE '" + value + "%'";
            case ENDS_WITH -> column + " LIKE '%" + value + "'";
            case EXISTS -> {
                if (filter.getSubquery() == null) {
                    throw new IllegalArgumentException("EXISTS requires a subquery");
                }
                yield "EXISTS (" + renderSubquerySql(filter.getSubquery().getQuery()) + ")";
            }
            case NOT_EXISTS -> {
                if (filter.getSubquery() == null) {
                    throw new IllegalArgumentException("NOT_EXISTS requires a subquery");
                }
                yield "NOT EXISTS (" + renderSubquerySql(filter.getSubquery().getQuery()) + ")";
            }
        };
    }

    // =================================================================
    // FROM + JOINS
    // =================================================================
    private String buildFromClause(DataRequest request) {
        String base;
        if (request.getFrom() != null) {
            base = renderFromSource(request.getFrom());
        } else {
            base = request.getEntity();      // legacy path
        }

        StringBuilder sb = new StringBuilder("FROM ").append(base);

        if (request.getJoins() != null) {
            for (JoinClause join : request.getJoins()) {
                sb.append(" ").append(renderJoin(join));
            }
        }
        return sb.toString();
    }

    private String renderFromSource(FromClause from) {
        if (from.getSubquery() != null) {
            if (from.getAlias() == null || from.getAlias().isBlank()) {
                throw new IllegalArgumentException("Alias is required for subquery in FROM clause");
            }
            return "(" + renderSubquerySql(from.getSubquery().getQuery()) + ") " + from.getAlias();
        }
        String sql = from.getEntity();
        if (from.getAlias() != null && !from.getAlias().isBlank()) {
            sql += " " + from.getAlias();
        }
        return sql;
    }

    private String renderJoin(JoinClause join) {
        String joinKeyword = switch (join.getType()) {
            case INNER -> "INNER JOIN";
            case LEFT  -> "LEFT JOIN";
            case RIGHT -> "RIGHT JOIN";
            case FULL  -> "FULL JOIN";
            case CROSS -> "CROSS JOIN";
        };

        String source;
        if (join.getSubquery() != null) {
            if (join.getAlias() == null || join.getAlias().isBlank()) {
                throw new IllegalArgumentException("Alias is required for joined subquery");
            }
            source = "(" + renderSubquerySql(join.getSubquery().getQuery()) + ") " + join.getAlias();
        } else {
            source = join.getEntity();
            if (join.getAlias() != null && !join.getAlias().isBlank()) {
                source += " " + join.getAlias();
            }
        }

        StringBuilder sb = new StringBuilder(joinKeyword).append(" ").append(source);
        if (join.getType() != JoinClause.JoinType.CROSS
                && join.getOn() != null && !join.getOn().isEmpty()) {
            sb.append(" ON ").append(renderJoinConditions(join.getOn()));
        }
        return sb.toString();
    }

    private String renderJoinConditions(List<JoinCondition> conditions) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < conditions.size(); i++) {
            JoinCondition c = conditions.get(i);
            if (i > 0) {
                sb.append(c.getLogicalOperator() == Filter.LogicalOperator.OR ? " OR " : " AND ");
            }
            sb.append(renderJoinCondition(c));
        }
        return sb.toString();
    }

    private String renderJoinCondition(JoinCondition c) {
        if (c.getOperator() == Filter.Operator.IS_NULL) {
            return c.getLeft() + " IS NULL";
        }
        if (c.getOperator() == Filter.Operator.IS_NOT_NULL) {
            return c.getLeft() + " IS NOT NULL";
        }
        String rhs = (c.getRight() != null) ? c.getRight() : literal(c.getValue());
        return c.getLeft() + " " + operatorSymbol(c.getOperator()) + " " + rhs;
    }

    // =================================================================
    // SUBQUERY (recursive)
    // =================================================================
    private String renderSubquerySql(DataRequest sub) {
        if (sub == null) {
            throw new IllegalArgumentException("Subquery definition is required");
        }
        if (sub.getOperation() != null && sub.getOperation() != DataRequest.Operation.READ) {
            throw new IllegalArgumentException("Only READ is allowed inside subqueries");
        }

        String selectClause  = buildSelectClause(sub);
        String fromClause    = buildFromClause(sub);
        String whereClause   = buildWhereClause(sub);
        String groupBy       = buildGroupByClause(sub.getGroupBy());
        String having        = buildHavingClause(sub.getHaving());
        String orderBy       = buildOrderByClause(sub.getOrderBy());

        StringBuilder sql = new StringBuilder("SELECT ").append(selectClause);
        sql.append(" ").append(fromClause);
        if (!whereClause.isEmpty()) sql.append(" WHERE ").append(whereClause);
        if (!groupBy.isEmpty())     sql.append(" GROUP BY ").append(groupBy);
        if (!having.isEmpty())      sql.append(" HAVING ").append(having);
        if (!orderBy.isEmpty())     sql.append(" ORDER BY ").append(orderBy);
        return sql.toString();
    }

    // =================================================================
    // HELPERS
    // =================================================================
    private String operatorSymbol(Filter.Operator op) {
        return switch (op) {
            case EQ  -> "=";
            case NEQ -> "<>";
            case GT  -> ">";
            case GTE -> ">=";
            case LT  -> "<";
            case LTE -> "<=";
            default -> throw new IllegalArgumentException(
                    "Operator " + op + " is not supported in column-to-column / JOIN context");
        };
    }

    private String literal(Object value) {
        if (value == null) return "NULL";
        if (value instanceof String) return "'" + value.toString().replace("'", "''") + "'";
        if (value instanceof Boolean) return ((Boolean) value) ? "TRUE" : "FALSE";
        return value.toString();
    }

    // =================================================================
    // GROUP BY / HAVING / ORDER BY / LIMIT  (unchanged)
    // =================================================================
    private String buildGroupByClause(List<GroupBy> groupByList) {
        if (groupByList == null || groupByList.isEmpty()) return "";

        List<String> groupColumns = new ArrayList<>();
        for (GroupBy groupBy : groupByList) {
            String column = groupBy.getColumn();
            String type = groupBy.getType() != null ? groupBy.getType().toString() : "NORMAL";
            String format = groupBy.getFormat();
            String alias = groupBy.getAlias();

            String expression = switch (type) {
                case "YEAR"    -> "EXTRACT(YEAR FROM " + column + ")";
                case "MONTH"   -> "EXTRACT(MONTH FROM " + column + ")";
                case "DAY"     -> "EXTRACT(DAY FROM " + column + ")";
                case "HOUR"    -> "EXTRACT(HOUR FROM " + column + ")";
                case "WEEK"    -> "EXTRACT(WEEK FROM " + column + ")";
                case "QUARTER" -> "EXTRACT(QUARTER FROM " + column + ")";
                case "DATE_TRUNC" -> {
                    String fmt = format != null ? format : "day";
                    yield switch (fmt.toLowerCase()) {
                        case "year"  -> "DATE_TRUNC('year', " + column + ")";
                        case "month" -> "DATE_TRUNC('month', " + column + ")";
                        case "day"   -> "DATE_TRUNC('day', " + column + ")";
                        case "hour"  -> "DATE_TRUNC('hour', " + column + ")";
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
        if (havingList == null || havingList.isEmpty()) return "";

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
                default -> throw new IllegalArgumentException(
                        "Operator " + having.getOperator() + " is not supported in HAVING");
            };

            havingConditions.add(condition);
        }
        return String.join(" AND ", havingConditions);
    }

    private String buildOrderByClause(List<OrderBy> orderByList) {
        if (orderByList == null || orderByList.isEmpty()) return "";

        List<String> orderClauses = new ArrayList<>();
        for (OrderBy orderBy : orderByList) {
            String direction = orderBy.getDirection() == OrderBy.Direction.DESC ? "DESC" : "ASC";
            orderClauses.add(orderBy.getColumn() + " " + direction);
        }
        return String.join(", ", orderClauses);
    }

    private String buildLimitClause(Integer limit, Integer offset) {
        if (limit == null || limit <= 0) return "";
        if (offset != null && offset >= 0) return "LIMIT " + limit + " OFFSET " + offset;
        return "LIMIT " + limit;
    }
}