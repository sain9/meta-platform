package com.hussain.data_api.grpc.interceptor;

import com.hussain.data_api.grpc.proto.DataRequest;
import io.grpc.ForwardingServerCall;
import io.grpc.ForwardingServerCallListener;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.Status;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.interceptor.GrpcGlobalServerInterceptor;
import org.springframework.beans.factory.annotation.Value;

/**
 * Logs every gRPC call: method, entity (for DataRequest), duration, final status.
 * Enabled by default; can be disabled with data.api.grpc.logging.enabled=false.
 *
 * Exception-safe: any failure inside the interceptor is swallowed so the
 * call itself never fails because of logging.
 */
@GrpcGlobalServerInterceptor
@Slf4j
public class ServerLoggingInterceptor implements ServerInterceptor {

    @Value("${data.api.grpc.logging.enabled:true}")
    private boolean enabled;

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            Metadata headers,
            ServerCallHandler<ReqT, RespT> next) {

        if (!enabled) {
            return next.startCall(call, headers);
        }

        long start = System.nanoTime();
        String method = call.getMethodDescriptor().getFullMethodName();

        // Wrap the call so we can capture the final status
        ServerCall<ReqT, RespT> wrappedCall = new ForwardingServerCall.SimpleForwardingServerCall<>(call) {
            @Override
            public void close(Status status, Metadata trailers) {
                long durationMs = (System.nanoTime() - start) / 1_000_000;
                if (status.isOk()) {
                    log.info("[gRPC-IN] {} OK in {}ms", method, durationMs);
                } else {
                    log.warn("[gRPC-IN] {} {} in {}ms — {}",
                            method, status.getCode(), durationMs,
                            status.getDescription() != null ? status.getDescription() : "");
                }
                super.close(status, trailers);
            }
        };

        ServerCall.Listener<ReqT> listener = next.startCall(wrappedCall, headers);

        // Enrich log with `entity` when the request arrives
        return new ForwardingServerCallListener.SimpleForwardingServerCallListener<>(listener) {
            @Override
            public void onMessage(ReqT message) {
                try {
                    if (message instanceof DataRequest req) {
                        log.info("[gRPC-IN] {} <- operation={}, entity={}",
                                method, req.getOperation(), req.getEntity());
                    }
                } catch (Throwable ignored) {
                    // Never let logging break the call
                }
                super.onMessage(message);
            }

            @Override
            public void onHalfClose() {
                try {
                    super.onHalfClose();
                } catch (Throwable t) {
                    long durationMs = (System.nanoTime() - start) / 1_000_000;
                    log.error("[gRPC-IN] {} THREW after {}ms", method, durationMs, t);
                    throw t;
                }
            }
        };
    }
}