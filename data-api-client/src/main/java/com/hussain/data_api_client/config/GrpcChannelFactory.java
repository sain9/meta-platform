package com.hussain.data_api_client.config;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class GrpcChannelFactory {

    private final DataApiClientProperties props;
    private final ObjectProvider<io.grpc.ClientInterceptor> interceptors;

    private volatile ManagedChannel channel;

    public ManagedChannel getChannel() {
        if (channel == null) {
            synchronized (this) {
                if (channel == null) {
                    channel = buildChannel();
                }
            }
        }
        return channel;
    }

    private ManagedChannel buildChannel() {
        String target = props.getHost() + ":" + props.getPort();
        ManagedChannelBuilder<?> builder = ManagedChannelBuilder.forTarget(target);
        if (props.isPlaintext()) {
            builder.usePlaintext();
        }
        builder.maxInboundMessageSize(props.getMaxInboundMessageSize());
        interceptors.orderedStream().forEach(builder::intercept);
        log.info("Opening gRPC channel to data-api at {}", target);
        return builder.build();
    }

    @PreDestroy
    public void shutdown() throws InterruptedException {
        if (channel != null) {
            channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
            log.info("Closed gRPC channel to data-api");
        }
    }
}