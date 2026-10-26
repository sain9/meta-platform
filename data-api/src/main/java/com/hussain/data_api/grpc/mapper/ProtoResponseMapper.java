package com.hussain.data_api.grpc.mapper;

import com.hussain.data_api.grpc.proto.DataResponse;
import com.hussain.data_api.grpc.proto.Pagination;
import com.hussain.data_api.grpc.proto.ValidationError;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ProtoResponseMapper {

    private final ValueConverter valueConverter;

    public DataResponse toProto(com.hussain.data_api.model.DataResponse<?> javaResp) {
        DataResponse.Builder b = DataResponse.newBuilder();
        if (javaResp == null) {
            return b.setSuccess(false).setMessage("Null response from DataService").build();
        }
        b.setSuccess(javaResp.isSuccess());
        if (javaResp.getMessage() != null) {
            b.setMessage(javaResp.getMessage());
        }
        if (javaResp.getData() != null) {
            b.setData(valueConverter.toProto(javaResp.getData()));
        }
        if (javaResp.getPagination() != null) {
            b.setPagination(Pagination.newBuilder()
                    .setLimit(javaResp.getPagination().getLimit())
                    .setOffset(javaResp.getPagination().getOffset())
                    .setTotalCount(javaResp.getPagination().getTotalCount())
                    .setTotalPages(javaResp.getPagination().getTotalPages())
                    .build());
        }
        if (javaResp.getErrors() != null) {
            for (var e : javaResp.getErrors()) {
                b.addErrors(ValidationError.newBuilder()
                        .setField(nullSafe(e.getField()))
                        .setMessage(nullSafe(e.getMessage()))
                        .setRejectedValue(e.getRejectedValue() == null
                                ? nullSafeProto()
                                : valueConverter.toProto(e.getRejectedValue()))
                        .build());
            }
        }
        return b.build();
    }

    private com.hussain.data_api.grpc.proto.Value nullSafeProto() {
        return com.hussain.data_api.grpc.proto.Value.newBuilder().setNullValue(true).build();
    }

    private String nullSafe(String s) {
        return s == null ? "" : s;
    }
}