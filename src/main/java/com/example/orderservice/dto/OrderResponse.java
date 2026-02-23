package com.example.orderservice.dto;

import com.example.orderservice.entity.OrderStatus;

import java.math.BigDecimal;

public class OrderResponse {

    private Long id;
    private Long userId;
    private Long productId;
    private Integer quantity;
    private BigDecimal totalAmount;
    private OrderStatus status;

    public OrderResponse(Long id,
                         Long userId,
                         Long productId,
                         Integer quantity,
                         BigDecimal totalAmount,
                         OrderStatus status) {
        this.id = id;
        this.userId = userId;
        this.productId = productId;
        this.quantity = quantity;
        this.totalAmount = totalAmount;
        this.status = status;
    }
}