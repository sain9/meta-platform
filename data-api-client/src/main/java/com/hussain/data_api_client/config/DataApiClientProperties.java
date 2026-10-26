package com.hussain.data_api_client.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "data-api.grpc")
public class DataApiClientProperties {
    /** gRPC server host */
    private String host = "localhost";
    /** gRPC server port */
    private int port = 9003;
    /** Enable plaintext (no TLS). For internal service-mesh use this is typically true. */
    private boolean plaintext = true;
    /** Default per-call deadline in seconds (0 = no timeout). */
    private long deadlineSeconds = 30;
    /** Max inbound message size (bytes). Default 8MB. */
    private int maxInboundMessageSize = 8 * 1024 * 1024;
    /** Enable the client (set to false in tests to avoid starting a channel). */
    private boolean enabled = true;
    /** Enable outbound gRPC logging. */
    private boolean loggingEnabled = true;
}