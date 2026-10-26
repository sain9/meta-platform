package com.hussain.data_api.grpc.mapper;

import com.google.protobuf.Int32Value;
import com.hussain.data_api.grpc.proto.DataRequest;
import com.hussain.data_api.grpc.proto.Filter;
import com.hussain.data_api.grpc.proto.FilterGroup;
import com.hussain.data_api.grpc.proto.FromClause;
import com.hussain.data_api.grpc.proto.GroupBy;
import com.hussain.data_api.grpc.proto.Having;
import com.hussain.data_api.grpc.proto.JoinClause;
import com.hussain.data_api.grpc.proto.JoinCondition;
import com.hussain.data_api.grpc.proto.OrderBy;
import com.hussain.data_api.grpc.proto.SubqueryRef;
import com.hussain.data_api.grpc.proto.Value;
import com.hussain.data_api.model.Filter.Operator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Translates proto DataRequest -> Java DataRequest.
 * Recursively handles from/joins/subqueries/filterGroup.
 */
@Component
@RequiredArgsConstructor
public class ProtoRequestMapper {

    private final ValueConverter valueConverter;

    public com.hussain.data_api.model.DataRequest toJava(DataRequest proto) {
        if (proto == null) {
            return null;
        }
        com.hussain.data_api.model.DataRequest out = new com.hussain.data_api.model.DataRequest();

        // Operation
        if (proto.getOperation() != com.hussain.data_api.grpc.proto.Operation.OPERATION_UNSPECIFIED) {
            out.setOperation(mapOperation(proto.getOperation()));
        }

        // Plain scalars
        if (!proto.getEntity().isEmpty()) {
            out.setEntity(proto.getEntity());
        }
        if (proto.getColumnsCount() > 0) {
            out.setColumns(new ArrayList<>(proto.getColumnsList()));
        }
        if (proto.hasLimit()) {
            out.setLimit(proto.getLimit().getValue());
        }
        if (proto.hasOffset()) {
            out.setOffset(proto.getOffset().getValue());
        }

        if (!proto.getBulkKeyColumn().isEmpty()) {
            out.setBulkKeyColumn(proto.getBulkKeyColumn());
        }

        // Filters
        if (proto.getFiltersCount() > 0) {
            out.setFilters(proto.getFiltersList().stream()
                    .map(this::toJava)
                    .collect(Collectors.toList()));
        }

        // Order by
        if (proto.getOrderByCount() > 0) {
            out.setOrderBy(proto.getOrderByList().stream()
                    .map(this::toJava)
                    .collect(Collectors.toList()));
        }

        // Group by
        if (proto.getGroupByCount() > 0) {
            out.setGroupBy(proto.getGroupByList().stream()
                    .map(this::toJava)
                    .collect(Collectors.toList()));
        }

        // Having
        if (proto.getHavingCount() > 0) {
            out.setHaving(proto.getHavingList().stream()
                    .map(this::toJava)
                    .collect(Collectors.toList()));
        }

        // From
        if (proto.hasFrom()) {
            out.setFrom(toJava(proto.getFrom()));
        }

        // Joins
        if (proto.getJoinsCount() > 0) {
            out.setJoins(proto.getJoinsList().stream()
                    .map(this::toJava)
                    .collect(Collectors.toList()));
        }

        // Filter group
        if (proto.hasFilterGroup()) {
            out.setFilterGroup(toJava(proto.getFilterGroup()));
        }

        // Data map
        if (!proto.getDataMap().isEmpty()) {
            Map<String, Object> data = new HashMap<>();
            proto.getDataMap().forEach((k, v) -> data.put(k, valueConverter.fromProto(v)));
            out.setData(data);
        }

        return out;
    }

    // ------------------------------------------------------------------
    // Operation
    // ------------------------------------------------------------------
    private com.hussain.data_api.model.DataRequest.Operation mapOperation(
            com.hussain.data_api.grpc.proto.Operation op) {
        return switch (op) {
            case READ -> com.hussain.data_api.model.DataRequest.Operation.READ;
            case CREATE -> com.hussain.data_api.model.DataRequest.Operation.CREATE;
            case UPDATE -> com.hussain.data_api.model.DataRequest.Operation.UPDATE;
            case DELETE -> com.hussain.data_api.model.DataRequest.Operation.DELETE;
            case BULK_CREATE -> com.hussain.data_api.model.DataRequest.Operation.BULK_CREATE;
            case BULK_UPDATE -> com.hussain.data_api.model.DataRequest.Operation.BULK_UPDATE;
            case BULK_DELETE -> com.hussain.data_api.model.DataRequest.Operation.BULK_DELETE;
            case OPERATION_UNSPECIFIED, UNRECOGNIZED -> null;
        };
    }

    // ------------------------------------------------------------------
    // From
    // ------------------------------------------------------------------
    private com.hussain.data_api.model.FromClause toJava(FromClause proto) {
        com.hussain.data_api.model.FromClause out = new com.hussain.data_api.model.FromClause();
        if (!proto.getEntity().isEmpty()) out.setEntity(proto.getEntity());
        if (proto.hasSubquery()) out.setSubquery(toJava(proto.getSubquery()));
        if (!proto.getAlias().isEmpty()) out.setAlias(proto.getAlias());
        return out;
    }

    // ------------------------------------------------------------------
    // Join
    // ------------------------------------------------------------------
    private com.hussain.data_api.model.JoinClause toJava(JoinClause proto) {
        com.hussain.data_api.model.JoinClause out = new com.hussain.data_api.model.JoinClause();
        out.setType(mapJoinType(proto.getType()));
        if (!proto.getEntity().isEmpty()) out.setEntity(proto.getEntity());
        if (proto.hasSubquery()) out.setSubquery(toJava(proto.getSubquery()));
        if (!proto.getAlias().isEmpty()) out.setAlias(proto.getAlias());
        if (proto.getOnCount() > 0) {
            out.setOn(proto.getOnList().stream()
                    .map(this::toJava)
                    .collect(Collectors.toList()));
        }
        return out;
    }

    private com.hussain.data_api.model.JoinClause.JoinType mapJoinType(
            com.hussain.data_api.grpc.proto.JoinType t) {
        return switch (t) {
            case INNER -> com.hussain.data_api.model.JoinClause.JoinType.INNER;
            case LEFT -> com.hussain.data_api.model.JoinClause.JoinType.LEFT;
            case RIGHT -> com.hussain.data_api.model.JoinClause.JoinType.RIGHT;
            case FULL -> com.hussain.data_api.model.JoinClause.JoinType.FULL;
            case CROSS -> com.hussain.data_api.model.JoinClause.JoinType.CROSS;
            case JOIN_TYPE_UNSPECIFIED, UNRECOGNIZED -> null;
        };
    }

    private com.hussain.data_api.model.JoinCondition toJava(JoinCondition proto) {
        com.hussain.data_api.model.JoinCondition out = new com.hussain.data_api.model.JoinCondition();
        if (!proto.getLeft().isEmpty()) out.setLeft(proto.getLeft());
        if (proto.getOperator() != com.hussain.data_api.grpc.proto.FilterOperator.FILTER_OPERATOR_UNSPECIFIED) {
            out.setOperator(mapFilterOperator(proto.getOperator()));
        }
        if (!proto.getRight().isEmpty()) out.setRight(proto.getRight());
        if (proto.hasValue()) out.setValue(valueConverter.fromProto(proto.getValue()));
        if (proto.getLogicalOperator() != com.hussain.data_api.grpc.proto.LogicalOperator.LOGICAL_OPERATOR_UNSPECIFIED) {
            out.setLogicalOperator(mapLogicalOperator(proto.getLogicalOperator()));
        }
        return out;
    }

    // ------------------------------------------------------------------
    // Filter
    // ------------------------------------------------------------------
    private com.hussain.data_api.model.Filter toJava(Filter proto) {
        com.hussain.data_api.model.Filter out = new com.hussain.data_api.model.Filter();
        if (!proto.getColumn().isEmpty()) out.setColumn(proto.getColumn());
        if (proto.getOperator() != com.hussain.data_api.grpc.proto.FilterOperator.FILTER_OPERATOR_UNSPECIFIED) {
            out.setOperator(mapFilterOperator(proto.getOperator()));
        }
        if (proto.hasValue()) out.setValue(valueConverter.fromProto(proto.getValue()));
        if (proto.getLogicalOperator() != com.hussain.data_api.grpc.proto.LogicalOperator.LOGICAL_OPERATOR_UNSPECIFIED) {
            out.setLogicalOperator(mapLogicalOperator(proto.getLogicalOperator()));
        }
        if (proto.hasSubquery()) out.setSubquery(toJava(proto.getSubquery()));
        if (!proto.getRightColumn().isEmpty()) out.setRightColumn(proto.getRightColumn());
        return out;
    }

    private Operator mapFilterOperator(com.hussain.data_api.grpc.proto.FilterOperator op) {
        return switch (op) {
            case EQ -> Operator.EQ;
            case NEQ -> Operator.NEQ;
            case GT -> Operator.GT;
            case GTE -> Operator.GTE;
            case LT -> Operator.LT;
            case LTE -> Operator.LTE;
            case LIKE -> Operator.LIKE;
            case ILIKE -> Operator.ILIKE;
            case IN -> Operator.IN;
            case NOT_IN -> Operator.NOT_IN;
            case IS_NULL -> Operator.IS_NULL;
            case IS_NOT_NULL -> Operator.IS_NOT_NULL;
            case BETWEEN -> Operator.BETWEEN;
            case CONTAINS -> Operator.CONTAINS;
            case STARTS_WITH -> Operator.STARTS_WITH;
            case ENDS_WITH -> Operator.ENDS_WITH;
            case EXISTS -> Operator.EXISTS;
            case NOT_EXISTS -> Operator.NOT_EXISTS;
            case FILTER_OPERATOR_UNSPECIFIED, UNRECOGNIZED -> null;
        };
    }

    private com.hussain.data_api.model.Filter.LogicalOperator mapLogicalOperator(
            com.hussain.data_api.grpc.proto.LogicalOperator op) {
        return switch (op) {
            case AND -> com.hussain.data_api.model.Filter.LogicalOperator.AND;
            case OR -> com.hussain.data_api.model.Filter.LogicalOperator.OR;
            case LOGICAL_OPERATOR_UNSPECIFIED, UNRECOGNIZED -> null;
        };
    }

    // ------------------------------------------------------------------
    // Filter group
    // ------------------------------------------------------------------
    private com.hussain.data_api.model.FilterGroup toJava(FilterGroup proto) {
        com.hussain.data_api.model.FilterGroup out = new com.hussain.data_api.model.FilterGroup();
        if (proto.getLogicalOperator() != com.hussain.data_api.grpc.proto.LogicalOperator.LOGICAL_OPERATOR_UNSPECIFIED) {
            out.setLogicalOperator(switch (proto.getLogicalOperator()) {
                case AND -> com.hussain.data_api.model.FilterGroup.LogicalOperator.AND;
                case OR -> com.hussain.data_api.model.FilterGroup.LogicalOperator.OR;
                case LOGICAL_OPERATOR_UNSPECIFIED, UNRECOGNIZED -> null;
            });
        }
        if (proto.getFiltersCount() > 0) {
            out.setFilters(proto.getFiltersList().stream()
                    .map(this::toJava)
                    .collect(Collectors.toList()));
        }
        if (proto.getGroupsCount() > 0) {
            out.setGroups(proto.getGroupsList().stream()
                    .map(this::toJava)
                    .collect(Collectors.toList()));
        }
        return out;
    }

    // ------------------------------------------------------------------
    // Subquery
    // ------------------------------------------------------------------
    private com.hussain.data_api.model.SubqueryRef toJava(SubqueryRef proto) {
        com.hussain.data_api.model.SubqueryRef out = new com.hussain.data_api.model.SubqueryRef();
        if (proto.hasQuery()) out.setQuery(toJava(proto.getQuery()));
        if (!proto.getAlias().isEmpty()) out.setAlias(proto.getAlias());
        return out;
    }

    // ------------------------------------------------------------------
    // Order by / Group by / Having
    // ------------------------------------------------------------------
    private com.hussain.data_api.model.OrderBy toJava(OrderBy proto) {
        com.hussain.data_api.model.OrderBy out = new com.hussain.data_api.model.OrderBy();
        if (!proto.getColumn().isEmpty()) out.setColumn(proto.getColumn());
        out.setDirection(switch (proto.getDirection()) {
            case DESC -> com.hussain.data_api.model.OrderBy.Direction.DESC;
            default -> com.hussain.data_api.model.OrderBy.Direction.ASC;
        });
        return out;
    }

    private com.hussain.data_api.model.GroupBy toJava(GroupBy proto) {
        com.hussain.data_api.model.GroupBy out = new com.hussain.data_api.model.GroupBy();
        if (!proto.getColumn().isEmpty()) out.setColumn(proto.getColumn());
        if (!proto.getAlias().isEmpty()) out.setAlias(proto.getAlias());
        if (proto.getType() != com.hussain.data_api.grpc.proto.GroupByType.GROUP_BY_TYPE_UNSPECIFIED) {
            out.setType(switch (proto.getType()) {
                case NORMAL -> com.hussain.data_api.model.GroupBy.GroupByType.NORMAL;
                case YEAR -> com.hussain.data_api.model.GroupBy.GroupByType.YEAR;
                case MONTH -> com.hussain.data_api.model.GroupBy.GroupByType.MONTH;
                case DAY -> com.hussain.data_api.model.GroupBy.GroupByType.DAY;
                case HOUR -> com.hussain.data_api.model.GroupBy.GroupByType.HOUR;
                case WEEK -> com.hussain.data_api.model.GroupBy.GroupByType.WEEK;
                case QUARTER -> com.hussain.data_api.model.GroupBy.GroupByType.QUARTER;
                case DATE_TRUNC -> com.hussain.data_api.model.GroupBy.GroupByType.DATE_TRUNC;
                case GROUP_BY_TYPE_UNSPECIFIED, UNRECOGNIZED -> null;
            });
        }
        if (!proto.getFormat().isEmpty()) out.setFormat(proto.getFormat());
        return out;
    }

    private com.hussain.data_api.model.Having toJava(Having proto) {
        com.hussain.data_api.model.Having out = new com.hussain.data_api.model.Having();
        if (!proto.getColumn().isEmpty()) out.setColumn(proto.getColumn());
        if (proto.getAggregate() != com.hussain.data_api.grpc.proto.AggregateFunction.AGGREGATE_FUNCTION_UNSPECIFIED) {
            out.setAggregate(switch (proto.getAggregate()) {
                case COUNT -> com.hussain.data_api.model.Having.AggregateFunction.COUNT;
                case SUM -> com.hussain.data_api.model.Having.AggregateFunction.SUM;
                case AVG -> com.hussain.data_api.model.Having.AggregateFunction.AVG;
                case MIN -> com.hussain.data_api.model.Having.AggregateFunction.MIN;
                case MAX -> com.hussain.data_api.model.Having.AggregateFunction.MAX;
                case COUNT_DISTINCT -> com.hussain.data_api.model.Having.AggregateFunction.COUNT_DISTINCT;
                case AGGREGATE_FUNCTION_UNSPECIFIED, UNRECOGNIZED -> null;
            });
        }
        if (proto.getOperator() != com.hussain.data_api.grpc.proto.HavingOperator.HAVING_OPERATOR_UNSPECIFIED) {
            out.setOperator(mapHavingOperator(proto.getOperator()));
        }
        if (proto.hasValue()) out.setValue(valueConverter.fromProto(proto.getValue()));
        if (proto.getLogicalOperator() != com.hussain.data_api.grpc.proto.LogicalOperator.LOGICAL_OPERATOR_UNSPECIFIED) {
            out.setLogicalOperator(mapHavingLogicalOp(proto.getLogicalOperator()));
        }
        return out;
    }

    private com.hussain.data_api.model.Having.Operator mapHavingOperator(
            com.hussain.data_api.grpc.proto.HavingOperator op) {
        return switch (op) {
            case HAVING_EQ -> com.hussain.data_api.model.Having.Operator.EQ;
            case HAVING_NEQ -> com.hussain.data_api.model.Having.Operator.NEQ;
            case HAVING_GT -> com.hussain.data_api.model.Having.Operator.GT;
            case HAVING_GTE -> com.hussain.data_api.model.Having.Operator.GTE;
            case HAVING_LT -> com.hussain.data_api.model.Having.Operator.LT;
            case HAVING_LTE -> com.hussain.data_api.model.Having.Operator.LTE;
            case HAVING_LIKE -> com.hussain.data_api.model.Having.Operator.LIKE;
            case HAVING_IN -> com.hussain.data_api.model.Having.Operator.IN;
            case HAVING_NOT_IN -> com.hussain.data_api.model.Having.Operator.NOT_IN;
            case HAVING_IS_NULL -> com.hussain.data_api.model.Having.Operator.IS_NULL;
            case HAVING_IS_NOT_NULL -> com.hussain.data_api.model.Having.Operator.IS_NOT_NULL;
            case HAVING_BETWEEN -> com.hussain.data_api.model.Having.Operator.BETWEEN;
            case HAVING_CONTAINS -> com.hussain.data_api.model.Having.Operator.CONTAINS;
            case HAVING_STARTS_WITH -> com.hussain.data_api.model.Having.Operator.STARTS_WITH;
            case HAVING_ENDS_WITH -> com.hussain.data_api.model.Having.Operator.ENDS_WITH;
            case HAVING_OPERATOR_UNSPECIFIED, UNRECOGNIZED -> null;
        };
    }

    private com.hussain.data_api.model.Having.LogicalOperator mapHavingLogicalOp(
            com.hussain.data_api.grpc.proto.LogicalOperator op) {
        return switch (op) {
            case AND -> com.hussain.data_api.model.Having.LogicalOperator.AND;
            case OR -> com.hussain.data_api.model.Having.LogicalOperator.OR;
            case LOGICAL_OPERATOR_UNSPECIFIED, UNRECOGNIZED -> null;
        };
    }
}