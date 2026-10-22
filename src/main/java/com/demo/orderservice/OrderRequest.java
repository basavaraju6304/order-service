package com.demo.orderservice;

public record OrderRequest(Integer productId, int quantity) {
}
