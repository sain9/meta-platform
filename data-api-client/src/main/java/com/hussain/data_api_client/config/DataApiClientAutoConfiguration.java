package com.hussain.data_api_client.config;

import com.hussain.data_api_client.DataApiClient;
import com.hussain.data_api_client.interceptor.ClientLoggingInterceptor;
import io.grpc.ClientInterceptor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@EnableConfigurationProperties(DataApiClientProperties.class)
@ConditionalOnProperty(prefix = "data-api.grpc", name = "enabled", havingValue = "true", matchIfMissing = true)
public class DataApiClientAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public GrpcChannelFactory grpcChannelFactory(DataApiClientProperties props,
                                                 ObjectProvider<ClientInterceptor> interceptors) {
        return new GrpcChannelFactory(props, interceptors);
    }

    @Bean
    @ConditionalOnMissingBean
    public DataApiClient dataApiClient(GrpcChannelFactory factory, DataApiClientProperties props) {
        return new DataApiClient(factory, props);
    }

    /** Logging interceptor — enabled by default; disable with data-api.grpc.logging-enabled=false */
    @Bean
    @ConditionalOnProperty(prefix = "data-api.grpc", name = "logging-enabled",
            havingValue = "true", matchIfMissing = true)
    public ClientLoggingInterceptor clientLoggingInterceptor() {
        return new ClientLoggingInterceptor();
    }
}