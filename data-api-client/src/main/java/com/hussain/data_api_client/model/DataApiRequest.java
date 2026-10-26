package com.hussain.data_api_client.model;

import com.hussain.data_api_client.model.enums.Aggregate;
import com.hussain.data_api_client.model.enums.Direction;
import com.hussain.data_api_client.model.enums.FilterOp;
import com.hussain.data_api_client.model.enums.JoinType;
import com.hussain.data_api_client.model.enums.LogicOp;
import com.hussain.data_api_client.model.enums.Operation;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class DataApiRequest {

    private Operation operation;
    private String entity;

    @Singular("column")     private List<String> columns;
    @Singular("filter")     private List<Filter> filters;
    @Singular("orderBy")    private List<OrderBy> orderBy;
    @Singular("groupBy")    private List<GroupBy> groupBy;
    @Singular("having")     private List<Having> having;

    private Integer limit;
    private Integer offset;

    // Advanced
    private From from;
    @Singular("join")       private List<Join> joins;
    private FilterGroup filterGroup;

    // For CREATE / UPDATE / BULK_*
    private Map<String, Object> data;

    // Only used by BULK_UPDATE
    private String bulkKeyColumn;

    // ---- convenience factories ----

    public static DataApiRequest read(String entity, String... columns) {
        return DataApiRequest.builder()
                .operation(Operation.READ)
                .entity(entity)
                .columns(Arrays.asList(columns))
                .build();
    }

    public static DataApiRequest create(String entity, Map<String, Object> data) {
        return DataApiRequest.builder()
                .operation(Operation.CREATE)
                .entity(entity)
                .data(data)
                .build();
    }

    public static DataApiRequest update(String entity, Map<String, Object> data) {
        return DataApiRequest.builder()
                .operation(Operation.UPDATE)
                .entity(entity)
                .data(data)
                .build();
    }

    public static DataApiRequest delete(String entity) {
        return DataApiRequest.builder()
                .operation(Operation.DELETE)
                .entity(entity)
                .build();
    }

    // ---- inner types ----

    @Data
    @Builder
    public static class Filter {
        private String column;
        private FilterOp operator;
        private Object value;
        private LogicOp logicalOperator;
        private Subquery subquery;
        private String rightColumn;
    }

    @Data
    @Builder
    public static class OrderBy {
        private String column;
        private Direction direction;
    }

    @Data
    @Builder
    public static class GroupBy {
        private String column;
        private String alias;
        private String type;
        private String format;
    }

    @Data
    @Builder
    public static class Having {
        private String column;
        private Aggregate aggregate;
        private FilterOp operator;
        private Object value;
        private LogicOp logicalOperator;
    }

    @Data
    @Builder
    public static class From {
        private String entity;
        private Subquery subquery;
        private String alias;
    }

    @Data
    @Builder
    public static class Join {
        private JoinType type;
        private String entity;
        private Subquery subquery;
        private String alias;
        @Singular("on")     private List<On> on;
    }

    @Data
    @Builder
    public static class On {
        private String left;
        private FilterOp operator;
        private String right;
        private Object value;
        private LogicOp logicalOperator;
    }

    @Data
    @Builder
    public static class Subquery {
        private DataApiRequest query;
        private String alias;
    }

    @Data
    @Builder
    public static class FilterGroup {
        private LogicOp logicalOperator;
        @Singular("filter")             private List<Filter> filters;
        @Singular("group")              private List<FilterGroup> groups;
    }
}