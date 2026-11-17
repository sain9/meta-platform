package com.hussain.product_service.controller;

import com.hussain.data_api_client.model.Row;
import com.hussain.product_service.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public List<Row> getAllProducts() {
        return productService.findAll();
    }

    @GetMapping("/{id}")
    public Row getProductById(@PathVariable Long id) {
        return productService.findById(id);
    }

    @PostMapping
    public int createProduct(@RequestBody Map<String, Object> data) {
        return productService.create(data);
    }

    @PutMapping("/{id}")
    public int updateProduct(@PathVariable Long id, @RequestBody Map<String, Object> data) {
        return productService.update(id, data);
    }

    @DeleteMapping("/{id}")
    public int deleteProduct(@PathVariable Long id) {
        return productService.delete(id);
    }
}
