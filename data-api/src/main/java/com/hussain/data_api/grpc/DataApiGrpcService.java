package com.hussain.data_api.grpc;

import com.hussain.data_api.grpc.mapper.ProtoRequestMapper;
import com.hussain.data_api.grpc.mapper.ProtoResponseMapper;
import com.hussain.data_api.grpc.proto.BatchExecuteRequest;
import com.hussain.data_api.grpc.proto.BatchExecuteResponse;
import com.hussain.data_api.grpc.proto.DataApiServiceGrpc;
import com.hussain.data_api.grpc.proto.DataRequest;
import com.hussain.data_api.grpc.proto.DataResponse;
import com.hussain.data_api.grpc.proto.InfoRequest;
import com.hussain.data_api.grpc.proto.InfoResponse;
import com.hussain.data_api.service.DataService;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Value;

import java.lang.management.ManagementFactory;
import java.util.stream.Collectors;

@GrpcService
@RequiredArgsConstructor
@Slf4j
public class DataApiGrpcService extends DataApiServiceGrpc.DataApiServiceImplBase {

    private final DataService dataService;
    private final ProtoRequestMapper requestMapper;
    private final ProtoResponseMapper responseMapper;

    @Value("${spring.application.name:data-api}")
    private String serviceName;

    @Value("${application.version:1.0.0}")
    private String serviceVersion;

    @Override
    public void info(InfoRequest request, StreamObserver<InfoResponse> responseObserver) {
        responseObserver.onNext(InfoResponse.newBuilder()
                .setService(serviceName)
                .setVersion(serviceVersion)
                .setStatus("UP")
                .setUptimeSeconds(ManagementFactory.getRuntimeMXBean().getUptime() / 1000)
                .build());
        responseObserver.onCompleted();
    }

    @Override
    public void execute(DataRequest request, StreamObserver<DataResponse> responseObserver) {
        try {
            log.info("gRPC Execute: operation={}, entity={}",
                    request.getOperation(), request.getEntity());

            var javaRequest = requestMapper.toJava(request);
            var javaResponse = dataService.processRequest(javaRequest);
            var protoResponse = responseMapper.toProto(javaResponse);

            responseObserver.onNext(protoResponse);
            responseObserver.onCompleted();
        } catch (IllegalArgumentException e) {
            log.warn("Invalid argument in gRPC Execute: {}", e.getMessage());
            responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription(e.getMessage())
                    .withCause(e)
                    .asRuntimeException());
        } catch (com.hussain.data_api.exception.DataApiException e) {
            log.error("DataApiException in gRPC Execute: {}", e.getMessage(), e);
            // Validation failures come wrapped in DataApiException with 500 status.
            // Re-classify them as INVALID_ARGUMENT for gRPC clients.
            String msg = e.getMessage() == null ? "" : e.getMessage();
            boolean isValidation = msg.contains("Validation failed")
                    || msg.contains("is required")
                    || msg.contains("Invalid")
                    || msg.contains("cannot exceed");
            Status status = isValidation ? Status.INVALID_ARGUMENT : Status.INTERNAL;
            responseObserver.onError(status
                    .withDescription(msg)
                    .withCause(e)
                    .asRuntimeException());
        }catch (Throwable t) {
            log.error("Unexpected error in gRPC Execute", t);
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Unexpected error: " + t.getMessage())
                    .withCause(t)
                    .asRuntimeException());
        }
    }

    @Override
    public void batchExecute(BatchExecuteRequest request,
                             StreamObserver<BatchExecuteResponse> responseObserver) {
        try {
            log.info("gRPC BatchExecute: size={}", request.getRequestsCount());

            var javaRequests = request.getRequestsList().stream()
                    .map(requestMapper::toJava)
                    .collect(Collectors.toList());
            var javaResponse = dataService.batchProcess(javaRequests);

            BatchExecuteResponse.Builder b = BatchExecuteResponse.newBuilder()
                    .setSuccess(javaResponse.isSuccess());
            if (javaResponse.getMessage() != null) b.setMessage(javaResponse.getMessage());

            // batchProcess returns DataResponse where data is a List<Object> — one
            // item per input request. We map each to its own DataResponse.
            if (javaResponse.getData() instanceof java.util.List<?> list) {
                for (Object item : list) {
                    b.addResults(toSingleItemResponse(item));
                }
            }
            responseObserver.onNext(b.build());
            responseObserver.onCompleted();
        } catch (IllegalArgumentException e) {
            responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription(e.getMessage()).withCause(e).asRuntimeException());
        } catch (Throwable t) {
            log.error("Unexpected error in gRPC BatchExecute", t);
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Unexpected error: " + t.getMessage())
                    .withCause(t).asRuntimeException());
        }
    }

    private DataResponse toSingleItemResponse(Object item) {
        com.hussain.data_api.model.DataResponse<Object> wrapper =
                com.hussain.data_api.model.DataResponse.builder()
                        .success(true)
                        .message("OK")
                        .data(item)
                        .build();
        return responseMapper.toProto(wrapper);
    }
}