package com.hussain.product_service.service;

import com.hussain.data_api_client.DataApiClient;
import com.hussain.data_api_client.model.DataApiRequest;
import com.hussain.data_api_client.model.Row;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final DataApiClient dataApiClient;
    private static final String ENTITY = "product";

    public List<Row> findAll() {
        return dataApiClient.read(DataApiRequest.builder()
                .operation(com.hussain.data_api_client.model.enums.Operation.READ)
                .entity(ENTITY)
                .build());
    }

    public Row findById(Long id) {
        List<Row> rows = dataApiClient.read(DataApiRequest.builder()
                .operation(com.hussain.data_api_client.model.enums.Operation.READ)
                .entity(ENTITY)
                .filter(DataApiRequest.Filter.builder()
                        .column("id")
                        .operator(com.hussain.data_api_client.model.enums.FilterOp.EQ)
                        .value(id)
                        .build())
                .build());
        return rows.isEmpty() ? null : rows.get(0);
    }

    public int create(Map<String, Object> data) {
        return dataApiClient.create(ENTITY, data);
    }

    public int update(Long id, Map<String, Object> data) {
        return dataApiClient.update(ENTITY, data, DataApiRequest.Filter.builder()
                .column("id")
                .operator(com.hussain.data_api_client.model.enums.FilterOp.EQ)
                .value(id)
                .build());
    }

    public int delete(Long id) {
        return dataApiClient.delete(ENTITY, DataApiRequest.Filter.builder()
                .column("id")
                .operator(com.hussain.data_api_client.model.enums.FilterOp.EQ)
                .value(id)
                .build());
    }
}
