package com.hussain.data_api_client;

import com.hussain.data_api_client.model.DataApiRequest;
import com.hussain.data_api_client.model.Row;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(classes = TestApplication.class)
@TestPropertySource(properties = {
        "data-api.grpc.host=localhost",
        "data-api.grpc.port=9003",
        "data-api.grpc.logging-enabled=true"
})
class ClientSpringContextTest {

    @Autowired
    private DataApiClient dataApiClient;

    @Test
    void read_viaAutoConfiguredClient() {
        List<Row> rows = dataApiClient.read(
                DataApiRequest.read("entity_store", "id", "table_name", "entity_definition"));
        assertFalse(rows.isEmpty());
        Row first = rows.get(0);
        assertNotNull(first.get("id"));
        System.out.println("AUTO-CONFIG OK: " + first);
    }
}