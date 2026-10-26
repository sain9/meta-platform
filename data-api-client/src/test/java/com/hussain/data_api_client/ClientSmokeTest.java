package com.hussain.data_api_client;

import com.hussain.data_api_client.config.DataApiClientProperties;
import com.hussain.data_api_client.config.GrpcChannelFactory;
import com.hussain.data_api_client.model.DataApiRequest;
import com.hussain.data_api_client.model.Row;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ClientSmokeTest {

    @Test
    void read_dnPricing() {
        DataApiClientProperties props = new DataApiClientProperties();
        props.setHost("localhost");
        props.setPort(9003);

        ObjectProvider<io.grpc.ClientInterceptor> noInterceptors = new ObjectProvider<>() {
            @Override public io.grpc.ClientInterceptor getObject(Object... args) {
                throw new org.springframework.beans.factory.NoSuchBeanDefinitionException(io.grpc.ClientInterceptor.class);
            }
            @Override public io.grpc.ClientInterceptor getObject() {
                throw new org.springframework.beans.factory.NoSuchBeanDefinitionException(io.grpc.ClientInterceptor.class);
            }
            @Override public io.grpc.ClientInterceptor getIfAvailable() { return null; }
            @Override public io.grpc.ClientInterceptor getIfUnique() { return null; }
            @Override public Iterator<io.grpc.ClientInterceptor> iterator() { return Collections.emptyIterator(); }
            @Override public Stream<io.grpc.ClientInterceptor> stream() { return Stream.empty(); }
        };

        DataApiClient client = new DataApiClient(new GrpcChannelFactory(props, noInterceptors), props);

        List<Row> rows = client.read(DataApiRequest.read("entity_store", "id", "table_name", "entity_definition"));
        assertFalse(rows.isEmpty(), "expected at least one row from entity_store");
        Row first = rows.get(0);
        assertNotNull(first.get("id"), "id column must be present");
        System.out.println("SMOKE OK: " + first);
    }
}