package com.demo.orderservice;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderRepository repository;
    private final RestClient restClient;
    private final String productServiceUrl;

    public OrderController(OrderRepository repository,
                           @Value("${product.service.url}") String productServiceUrl) {
        this.repository = repository;
        this.restClient = RestClient.create();
        this.productServiceUrl = productServiceUrl;
    }

    @GetMapping
    public List<Order> getAllOrders() {
        return repository.findAll();
    }

    @PostMapping
    public ResponseEntity<?> createOrder(@RequestBody OrderRequest request) {

        ProductResponse product;
        try {
            product = restClient.get()
                    .uri(productServiceUrl + "/products/{id}", request.productId())
                    .retrieve()
                    .body(ProductResponse.class);
        } catch (HttpClientErrorException.NotFound e) {
            return ResponseEntity.status(404)
                    .body("Product not found with id " + request.productId());
        }

        Order order = new Order();
        order.setProductId(product.id());
        order.setProductName(product.name());
        order.setQuantity(request.quantity());
        order.setTotalPrice(product.price() * request.quantity());

        return ResponseEntity.ok(repository.save(order));
    }
}