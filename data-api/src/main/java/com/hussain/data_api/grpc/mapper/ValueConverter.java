package com.hussain.data_api.grpc.mapper;

import com.google.protobuf.ByteString;
import com.hussain.data_api.grpc.proto.ListValue;
import com.hussain.data_api.grpc.proto.MapValue;
import com.hussain.data_api.grpc.proto.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Converts between proto {@link Value} (oneof) and Java's {@code Object}.
 *
 * Java -> proto rules:
 *   null             -> null_value = true
 *   String           -> string_value
 *   Long/Integer/Short/Byte -> int_value
 *   Double/Float     -> double_value
 *   Boolean          -> bool_value
 *   byte[]/ByteString -> bytes_value
 *   Collection<?>    -> list_value
 *   Map<?,?>         -> map_value  (keys stringified, values recursively converted)
 *   other            -> string_value (toString() fallback)
 *
 * proto -> Java rules (mirror of above, with numeric widening):
 *   null_value   -> null
 *   int_value    -> Long
 *   double_value -> Double
 *   bool_value   -> Boolean
 *   string_value -> String
 *   bytes_value  -> ByteString
 *   list_value   -> java.util.List<Object>
 *   map_value    -> java.util.LinkedHashMap<String,Object>  (order preserved)
 */
@Component
public class ValueConverter {

    // ------------------------------------------------------------------
    // proto -> Java
    // ------------------------------------------------------------------
    public Object fromProto(Value value) {
        if (value == null) {
            return null;
        }
        return switch (value.getKindCase()) {
            case NULL_VALUE -> null;
            case STRING_VALUE -> value.getStringValue();
            case INT_VALUE -> value.getIntValue();       // Java long
            case DOUBLE_VALUE -> value.getDoubleValue(); // Java double
            case BOOL_VALUE -> value.getBoolValue();
            case BYTES_VALUE -> value.getBytesValue();
            case LIST_VALUE -> listFromProto(value.getListValue());
            case MAP_VALUE -> mapFromProto(value.getMapValue());
            case KIND_NOT_SET -> null;
        };
    }

    public List<Object> listFromProto(ListValue listValue) {
        if (listValue == null) {
            return List.of();
        }
        List<Object> result = new ArrayList<>(listValue.getValuesCount());
        for (Value v : listValue.getValuesList()) {
            result.add(fromProto(v));
        }
        return result;
    }

    /**
     * Ordered map — preserves the column order of the SQL SELECT result.
     */
    public Map<String, Object> mapFromProto(MapValue mapValue) {
        if (mapValue == null) {
            return Map.of();
        }
        Map<String, Object> out = new LinkedHashMap<>();
        mapValue.getFieldsMap().forEach((k, v) -> out.put(k, fromProto(v)));
        return out;
    }

    // ------------------------------------------------------------------
    // Java -> proto
    // ------------------------------------------------------------------
    public Value toProto(Object obj) {
        Value.Builder b = Value.newBuilder();
        if (obj == null) {
            return b.setNullValue(true).build();
        }
        if (obj instanceof String s) {
            return b.setStringValue(s).build();
        }
        if (obj instanceof Long l) {
            return b.setIntValue(l).build();
        }
        if (obj instanceof Integer i) {
            return b.setIntValue(i.longValue()).build();
        }
        if (obj instanceof Short s) {
            return b.setIntValue(s.longValue()).build();
        }
        if (obj instanceof Byte by) {
            return b.setIntValue(by.longValue()).build();
        }
        if (obj instanceof Double d) {
            return b.setDoubleValue(d).build();
        }
        if (obj instanceof Float f) {
            return b.setDoubleValue(f.doubleValue()).build();
        }
        if (obj instanceof Boolean bool) {
            return b.setBoolValue(bool).build();
        }
        if (obj instanceof byte[] arr) {
            return b.setBytesValue(ByteString.copyFrom(arr)).build();
        }
        if (obj instanceof ByteString bs) {
            return b.setBytesValue(bs).build();
        }
        if (obj instanceof Collection<?> coll) {
            ListValue.Builder lv = ListValue.newBuilder();
            for (Object item : coll) {
                lv.addValues(toProto(item));
            }
            return b.setListValue(lv.build()).build();
        }
        if (obj instanceof Map<?, ?> map) {
            MapValue.Builder mv = MapValue.newBuilder();
            for (Map.Entry<?, ?> e : map.entrySet()) {
                mv.putFields(String.valueOf(e.getKey()), toProto(e.getValue()));
            }
            return b.setMapValue(mv.build()).build();
        }
        // Fallback for anything else (LocalDateTime, UUID, BigDecimal, etc.)
        return b.setStringValue(obj.toString()).build();
    }
}