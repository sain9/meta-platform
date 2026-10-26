package com.hussain.data_api_client.interceptor;

import com.hussain.data_api_client.config.DataApiClientProperties;
import com.hussain.data_api_client.proto.DataRequest;
import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.ForwardingClientCall;
import io.grpc.ForwardingClientCallListener;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.grpc.Status;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Logs every outgoing gRPC call: method, entity (when DataRequest),
 * duration, and final status. Enabled via data-api.grpc.logging.enabled=true
 * (auto-config registers the bean only when that property is true).
 *
 * Exception-safe: any failure inside the interceptor is swallowed.
 */
@Slf4j
@RequiredArgsConstructor
public class ClientLoggingInterceptor implements ClientInterceptor {

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
            MethodDescriptor<ReqT, RespT> method,
            CallOptions callOptions,
            Channel next) {

        long start = System.nanoTime();
        String fullMethod = method.getFullMethodName();

        return new ForwardingClientCall.SimpleForwardingClientCall<>(next.newCall(method, callOptions)) {
            @Override
            public void start(Listener<RespT> responseListener, Metadata headers) {
                Listener<RespT> wrapped = new ForwardingClientCallListener
                        .SimpleForwardingClientCallListener<>(responseListener) {
                    @Override
                    public void onClose(Status status, Metadata trailers) {
                        long durationMs = (System.nanoTime() - start) / 1_000_000;
                        if (status.isOk()) {
                            log.info("[gRPC-OUT] {} OK in {}ms", fullMethod, durationMs);
                        } else {
                            log.warn("[gRPC-OUT] {} {} in {}ms — {}",
                                    fullMethod, status.getCode(), durationMs,
                                    status.getDescription() != null ? status.getDescription() : "");
                        }
                        super.onClose(status, trailers);
                    }
                };
                super.start(wrapped, headers);
            }

            @Override
            public void sendMessage(ReqT message) {
                try {
                    if (message instanceof DataRequest req) {
                        log.info("[gRPC-OUT] {} -> operation={}, entity={}",
                                fullMethod, req.getOperation(), req.getEntity());
                    }
                } catch (Throwable ignored) {
                    // Never let logging break the call
                }
                super.sendMessage(message);
            }
        };
    }
}