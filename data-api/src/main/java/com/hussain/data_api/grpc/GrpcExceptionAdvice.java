package com.hussain.data_api.grpc;

import io.grpc.ForwardingServerCallListener;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.Status;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.interceptor.GrpcGlobalServerInterceptor;

@GrpcGlobalServerInterceptor
@Slf4j
public class GrpcExceptionAdvice implements ServerInterceptor {

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            Metadata headers,
            ServerCallHandler<ReqT, RespT> next) {
        ServerCall.Listener<ReqT> listener = next.startCall(call, headers);
        return new ForwardingServerCallListener.SimpleForwardingServerCallListener<>(listener) {
            @Override
            public void onHalfClose() {
                try {
                    super.onHalfClose();
                } catch (IllegalArgumentException e) {
                    log.warn("IllegalArgumentException in gRPC call", e);
                    call.close(Status.INVALID_ARGUMENT.withDescription(e.getMessage()).withCause(e),
                            new Metadata());
                } catch (Throwable t) {
                    log.error("Unexpected error in gRPC call", t);
                    call.close(Status.INTERNAL.withDescription("Internal error: " + t.getMessage())
                            .withCause(t), new Metadata());
                }
            }
        };
    }
}