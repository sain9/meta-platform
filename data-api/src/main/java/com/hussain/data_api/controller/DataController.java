package com.hussain.data_api.controller;

import com.hussain.data_api.model.DataRequest;
import com.hussain.data_api.model.DataResponse;
import com.hussain.data_api.service.DataService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/data")
@RequiredArgsConstructor
@Slf4j
public class DataController {

    private final DataService dataService;

    @PostMapping("/execute")
    public ResponseEntity<DataResponse<Object>> execute(@Valid @RequestBody DataRequest request) {
        log.info("Processing data request: operation={}, entity={}",
                request.getOperation(), request.getEntity());

        DataResponse<Object> response = dataService.processRequest(request);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/batch")
    public ResponseEntity<DataResponse<Object>> batchExecute(@Valid @RequestBody List<DataRequest> requests) {
        log.info("Processing batch request with {} operations", requests.size());

        DataResponse<Object> response = dataService.batchProcess(requests);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "UP", "service", "data-api"));
    }
}