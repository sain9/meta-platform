package com.hussain.data_api_client;

import com.hussain.data_api_client.config.DataApiClientProperties;
import com.hussain.data_api_client.config.GrpcChannelFactory;
import com.hussain.data_api_client.exception.DataApiClientException;
import com.hussain.data_api_client.model.BatchResult;
import com.hussain.data_api_client.model.DataApiRequest;
import com.hussain.data_api_client.model.DataApiResponse;
import com.hussain.data_api_client.model.Row;
import com.hussain.data_api_client.proto.BatchExecuteRequest;
import com.hussain.data_api_client.proto.BatchExecuteResponse;
import com.hussain.data_api_client.proto.DataApiServiceGrpc;
import com.hussain.data_api_client.proto.DataResponse;
import com.hussain.data_api_client.proto.InfoRequest;
import com.hussain.data_api_client.proto.InfoResponse;
import com.hussain.data_api_client.proto.ListValue;
import com.hussain.data_api_client.proto.MapValue;
import com.hussain.data_api_client.proto.Value;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
public class DataApiClient {

    private final GrpcChannelFactory channelFactory;
    private final DataApiClientProperties props;

    // -----------------------------------------------------------------
    // Public API
    // -----------------------------------------------------------------
    public DataApiResponse execute(DataApiRequest request) {
        return doCall(stub -> {
            com.hussain.data_api_client.proto.DataRequest proto = toProto(request);
            DataResponse response = stub.withDeadlineAfter(props.getDeadlineSeconds(), TimeUnit.SECONDS)
                    .execute(proto);
            return toJava(response);
        });
    }

    public List<Row> read(DataApiRequest request) {
        DataApiResponse r = execute(request);
        if (r.getRows() == null) {
            return List.of();
        }
        List<Row> out = new ArrayList<>(r.getRows().size());
        for (Map<String, Object> row : r.getRows()) {
            Row r2 = new Row();
            r2.putAll(row);
            out.add(r2);
        }
        return out;
    }

    public int create(String entity, Map<String, Object> data) {
        DataApiResponse r = execute(DataApiRequest.create(entity, data));
        return affectedRows(r);
    }

    public int update(String entity, Map<String, Object> data, DataApiRequest.Filter... filters) {
        DataApiRequest.DataApiRequestBuilder b = DataApiRequest.builder()
                .operation(com.hussain.data_api_client.model.enums.Operation.UPDATE)
                .entity(entity)
                .data(data);
        for (DataApiRequest.Filter f : filters) {
            b.filter(f);
        }
        return affectedRows(execute(b.build()));
    }

    public int delete(String entity, DataApiRequest.Filter... filters) {
        DataApiRequest.DataApiRequestBuilder b = DataApiRequest.builder()
                .operation(com.hussain.data_api_client.model.enums.Operation.DELETE)
                .entity(entity);
        for (DataApiRequest.Filter f : filters) {
            b.filter(f);
        }
        return affectedRows(execute(b.build()));
    }

    public BatchResult batch(List<DataApiRequest> requests) {
        return doCall(stub -> {
            BatchExecuteRequest.Builder b = BatchExecuteRequest.newBuilder();
            for (DataApiRequest r : requests) {
                b.addRequests(toProto(r));
            }
            BatchExecuteResponse resp = stub.withDeadlineAfter(props.getDeadlineSeconds(), TimeUnit.SECONDS)
                    .batchExecute(b.build());
            BatchResult out = new BatchResult();
            out.setSuccess(resp.getSuccess());
            out.setMessage(resp.getMessage());
            List<DataApiResponse> items = new ArrayList<>(resp.getResultsCount());
            for (DataResponse r : resp.getResultsList()) {
                items.add(toJava(r));
            }
            out.setResults(items);
            return out;
        });
    }

    public InfoResponse info() {
        return doCall(stub -> stub.withDeadlineAfter(5, TimeUnit.SECONDS)
                .info(InfoRequest.newBuilder().build()));
    }

    // -----------------------------------------------------------------
    // Internal helpers
    // -----------------------------------------------------------------
    private <T> T doCall(StubCall<T> call) {
        DataApiServiceGrpc.DataApiServiceBlockingStub stub =
                DataApiServiceGrpc.newBlockingStub(channelFactory.getChannel());
        try {
            return call.call(stub);
        } catch (StatusRuntimeException e) {
            Status.Code code = e.getStatus().getCode();
            String desc = e.getStatus().getDescription();
            log.warn("gRPC call failed: code={}, desc={}", code, desc);
            throw new DataApiClientException(code, desc != null ? desc : e.getMessage(), e);
        }
    }

    private int affectedRows(DataApiResponse r) {
        if (r.getData() instanceof Map<?, ?> m) {
            Object v = m.get("affectedRows");
            if (v instanceof Number n) return n.intValue();
        }
        return 0;
    }

    // -----------------------------------------------------------------
    // Proto <-> Java mapping
    // -----------------------------------------------------------------
    private com.hussain.data_api_client.proto.DataRequest toProto(DataApiRequest req) {
        com.hussain.data_api_client.proto.DataRequest.Builder b =
                com.hussain.data_api_client.proto.DataRequest.newBuilder();
        if (req.getOperation() != null) {
            b.setOperation(com.hussain.data_api_client.proto.Operation.valueOf(req.getOperation().name()));
        }
        if (req.getEntity() != null) b.setEntity(req.getEntity());
        if (req.getColumns() != null) b.addAllColumns(req.getColumns());
        if (req.getLimit() != null) {
            b.setLimit(com.google.protobuf.Int32Value.of(req.getLimit()));
        }
        if (req.getOffset() != null) {
            b.setOffset(com.google.protobuf.Int32Value.of(req.getOffset()));
        }
        if (req.getBulkKeyColumn() != null) b.setBulkKeyColumn(req.getBulkKeyColumn());
        if (req.getFilters() != null) {
            for (DataApiRequest.Filter f : req.getFilters()) b.addFilters(toProto(f));
        }
        if (req.getOrderBy() != null) {
            for (DataApiRequest.OrderBy o : req.getOrderBy()) b.addOrderBy(toProto(o));
        }
        if (req.getGroupBy() != null) {
            for (DataApiRequest.GroupBy g : req.getGroupBy()) b.addGroupBy(toProto(g));
        }
        if (req.getHaving() != null) {
            for (DataApiRequest.Having h : req.getHaving()) b.addHaving(toProto(h));
        }
        if (req.getFrom() != null) b.setFrom(toProto(req.getFrom()));
        if (req.getJoins() != null) {
            for (DataApiRequest.Join j : req.getJoins()) b.addJoins(toProto(j));
        }
        if (req.getFilterGroup() != null) b.setFilterGroup(toProto(req.getFilterGroup()));
        if (req.getData() != null) {
            req.getData().forEach((k, v) -> b.putData(k, toProtoValue(v)));
        }
        return b.build();
    }

    private com.hussain.data_api_client.proto.Filter toProto(DataApiRequest.Filter f) {
        com.hussain.data_api_client.proto.Filter.Builder b =
                com.hussain.data_api_client.proto.Filter.newBuilder();
        if (f.getColumn() != null) b.setColumn(f.getColumn());
        if (f.getOperator() != null) {
            b.setOperator(com.hussain.data_api_client.proto.FilterOperator.valueOf(f.getOperator().name()));
        }
        if (f.getValue() != null) b.setValue(toProtoValue(f.getValue()));
        if (f.getLogicalOperator() != null) {
            b.setLogicalOperator(com.hussain.data_api_client.proto.LogicalOperator.valueOf(f.getLogicalOperator().name()));
        }
        if (f.getSubquery() != null) {
            b.setSubquery(com.hussain.data_api_client.proto.SubqueryRef.newBuilder()
                    .setQuery(toProto(f.getSubquery().getQuery()))
                    .setAlias(f.getSubquery().getAlias() == null ? "" : f.getSubquery().getAlias())
                    .build());
        }
        if (f.getRightColumn() != null) b.setRightColumn(f.getRightColumn());
        return b.build();
    }

    private com.hussain.data_api_client.proto.OrderBy toProto(DataApiRequest.OrderBy o) {
        com.hussain.data_api_client.proto.OrderBy.Builder b =
                com.hussain.data_api_client.proto.OrderBy.newBuilder();
        if (o.getColumn() != null) b.setColumn(o.getColumn());
        if (o.getDirection() != null) {
            b.setDirection(com.hussain.data_api_client.proto.Direction.valueOf(o.getDirection().name()));
        }
        return b.build();
    }

    private com.hussain.data_api_client.proto.GroupBy toProto(DataApiRequest.GroupBy g) {
        com.hussain.data_api_client.proto.GroupBy.Builder b =
                com.hussain.data_api_client.proto.GroupBy.newBuilder();
        if (g.getColumn() != null) b.setColumn(g.getColumn());
        if (g.getAlias() != null) b.setAlias(g.getAlias());
        if (g.getType() != null) {
            b.setType(com.hussain.data_api_client.proto.GroupByType.valueOf(g.getType()));
        }
        if (g.getFormat() != null) b.setFormat(g.getFormat());
        return b.build();
    }

    private com.hussain.data_api_client.proto.Having toProto(DataApiRequest.Having h) {
        com.hussain.data_api_client.proto.Having.Builder b =
                com.hussain.data_api_client.proto.Having.newBuilder();
        if (h.getColumn() != null) b.setColumn(h.getColumn());
        if (h.getAggregate() != null) {
            b.setAggregate(com.hussain.data_api_client.proto.AggregateFunction.valueOf(h.getAggregate().name()));
        }
        if (h.getOperator() != null) {
            b.setOperator(com.hussain.data_api_client.proto.HavingOperator.valueOf("HAVING_" + h.getOperator().name()));
        }
        if (h.getValue() != null) b.setValue(toProtoValue(h.getValue()));
        if (h.getLogicalOperator() != null) {
            b.setLogicalOperator(com.hussain.data_api_client.proto.LogicalOperator.valueOf(h.getLogicalOperator().name()));
        }
        return b.build();
    }

    private com.hussain.data_api_client.proto.FromClause toProto(DataApiRequest.From from) {
        com.hussain.data_api_client.proto.FromClause.Builder b =
                com.hussain.data_api_client.proto.FromClause.newBuilder();
        if (from.getEntity() != null) b.setEntity(from.getEntity());
        if (from.getSubquery() != null) {
            b.setSubquery(com.hussain.data_api_client.proto.SubqueryRef.newBuilder()
                    .setQuery(toProto(from.getSubquery().getQuery()))
                    .setAlias(from.getSubquery().getAlias() == null ? "" : from.getSubquery().getAlias())
                    .build());
        }
        if (from.getAlias() != null) b.setAlias(from.getAlias());
        return b.build();
    }

    private com.hussain.data_api_client.proto.JoinClause toProto(DataApiRequest.Join j) {
        com.hussain.data_api_client.proto.JoinClause.Builder b =
                com.hussain.data_api_client.proto.JoinClause.newBuilder();
        if (j.getType() != null) {
            b.setType(com.hussain.data_api_client.proto.JoinType.valueOf(j.getType().name()));
        }
        if (j.getEntity() != null) b.setEntity(j.getEntity());
        if (j.getSubquery() != null) {
            b.setSubquery(com.hussain.data_api_client.proto.SubqueryRef.newBuilder()
                    .setQuery(toProto(j.getSubquery().getQuery()))
                    .setAlias(j.getSubquery().getAlias() == null ? "" : j.getSubquery().getAlias())
                    .build());
        }
        if (j.getAlias() != null) b.setAlias(j.getAlias());
        if (j.getOn() != null) {
            for (DataApiRequest.On on : j.getOn()) {
                com.hussain.data_api_client.proto.JoinCondition.Builder ob =
                        com.hussain.data_api_client.proto.JoinCondition.newBuilder();
                if (on.getLeft() != null) ob.setLeft(on.getLeft());
                if (on.getOperator() != null) {
                    ob.setOperator(com.hussain.data_api_client.proto.FilterOperator.valueOf(on.getOperator().name()));
                }
                if (on.getRight() != null) ob.setRight(on.getRight());
                if (on.getValue() != null) ob.setValue(toProtoValue(on.getValue()));
                if (on.getLogicalOperator() != null) {
                    ob.setLogicalOperator(com.hussain.data_api_client.proto.LogicalOperator.valueOf(on.getLogicalOperator().name()));
                }
                b.addOn(ob.build());
            }
        }
        return b.build();
    }

    private com.hussain.data_api_client.proto.FilterGroup toProto(DataApiRequest.FilterGroup g) {
        com.hussain.data_api_client.proto.FilterGroup.Builder b =
                com.hussain.data_api_client.proto.FilterGroup.newBuilder();
        if (g.getLogicalOperator() != null) {
            b.setLogicalOperator(com.hussain.data_api_client.proto.LogicalOperator.valueOf(g.getLogicalOperator().name()));
        }
        if (g.getFilters() != null) {
            for (DataApiRequest.Filter f : g.getFilters()) b.addFilters(toProto(f));
        }
        if (g.getGroups() != null) {
            for (DataApiRequest.FilterGroup sub : g.getGroups()) b.addGroups(toProto(sub));
        }
        return b.build();
    }

    // -----------------------------------------------------------------
    // Value
    // -----------------------------------------------------------------
    private Value toProtoValue(Object v) {
        Value.Builder b = Value.newBuilder();
        if (v == null) return b.setNullValue(true).build();
        if (v instanceof String s) return b.setStringValue(s).build();
        if (v instanceof Long l) return b.setIntValue(l).build();
        if (v instanceof Integer i) return b.setIntValue(i.longValue()).build();
        if (v instanceof Short s) return b.setIntValue(s.longValue()).build();
        if (v instanceof Double d) return b.setDoubleValue(d).build();
        if (v instanceof Float f) return b.setDoubleValue(f.doubleValue()).build();
        if (v instanceof Boolean bo) return b.setBoolValue(bo).build();
        if (v instanceof byte[] arr) return b.setBytesValue(com.google.protobuf.ByteString.copyFrom(arr)).build();
        if (v instanceof java.util.Collection<?> coll) {
            ListValue.Builder lv = ListValue.newBuilder();
            for (Object item : coll) lv.addValues(toProtoValue(item));
            return b.setListValue(lv.build()).build();
        }
        if (v instanceof Map<?, ?> map) {
            MapValue.Builder mv = MapValue.newBuilder();
            for (Map.Entry<?, ?> e : map.entrySet()) {
                mv.putFields(String.valueOf(e.getKey()), toProtoValue(e.getValue()));
            }
            return b.setMapValue(mv.build()).build();
        }
        return b.setStringValue(v.toString()).build();
    }

    private Object fromProtoValue(Value v) {
        return switch (v.getKindCase()) {
            case NULL_VALUE -> null;
            case STRING_VALUE -> v.getStringValue();
            case INT_VALUE -> v.getIntValue();
            case DOUBLE_VALUE -> v.getDoubleValue();
            case BOOL_VALUE -> v.getBoolValue();
            case BYTES_VALUE -> v.getBytesValue();
            case LIST_VALUE -> {
                List<Object> out = new ArrayList<>(v.getListValue().getValuesCount());
                for (Value item : v.getListValue().getValuesList()) out.add(fromProtoValue(item));
                yield out;
            }
            case MAP_VALUE -> {
                Map<String, Object> out = new LinkedHashMap<>();
                v.getMapValue().getFieldsMap().forEach((k, vv) -> out.put(k, fromProtoValue(vv)));
                yield out;
            }
            case KIND_NOT_SET -> null;
        };
    }

    // -----------------------------------------------------------------
    // Response mapping
    // -----------------------------------------------------------------
    private DataApiResponse toJava(DataResponse proto) {
        DataApiResponse out = new DataApiResponse();
        out.setSuccess(proto.getSuccess());
        out.setMessage(proto.getMessage());

        if (proto.hasData()) {
            Object decoded = fromProtoValue(proto.getData());
            // If decoded is List<Map>, treat as `rows`; else as `data`.
            if (decoded instanceof List<?> list && !list.isEmpty() && list.get(0) instanceof Map) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> rows = (List<Map<String, Object>>) list;
                out.setRows(rows);
            } else {
                out.setData(decoded);
            }
        }

        if (proto.hasPagination()) {
            DataApiResponse.Pagination p = new DataApiResponse.Pagination();
            p.setLimit(proto.getPagination().getLimit());
            p.setOffset(proto.getPagination().getOffset());
            p.setTotalCount(proto.getPagination().getTotalCount());
            p.setTotalPages(proto.getPagination().getTotalPages());
            out.setPagination(p);
        }

        if (proto.getErrorsCount() > 0) {
            List<DataApiResponse.ValidationError> errors = new ArrayList<>(proto.getErrorsCount());
            for (var e : proto.getErrorsList()) {
                DataApiResponse.ValidationError ve = new DataApiResponse.ValidationError();
                ve.setField(e.getField());
                ve.setMessage(e.getMessage());
                if (e.hasRejectedValue()) {
                    ve.setRejectedValue(fromProtoValue(e.getRejectedValue()));
                }
                errors.add(ve);
            }
            out.setErrors(errors);
        }

        return out;
    }

    @FunctionalInterface
    private interface StubCall<T> {
        T call(DataApiServiceGrpc.DataApiServiceBlockingStub stub);
    }
}