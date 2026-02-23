package com.example.orderservice.service;

import com.example.orderservice.dto.OrderRequest;
import com.example.orderservice.dto.OrderResponse;
import com.example.orderservice.dto.ProductDto;
import com.example.orderservice.entity.Order;
import com.example.orderservice.entity.OrderStatus;
import com.example.orderservice.repository.OrderRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import com.example.orderservice.client.ProductClient;
import com.example.orderservice.client.UserClient;

@Service
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository repository;
    private final UserClient userClient;
    private final ProductClient productClient;

    public OrderServiceImpl(OrderRepository repository,
                            UserClient userClient,
                            ProductClient productClient) {
        this.repository = repository;
        this.userClient = userClient;
        this.productClient = productClient;
    }

    @Override
    public OrderResponse createOrder(OrderRequest request) {

        // 1️⃣ Validate user
        userClient.getUserById(request.getUserId());

        // 2️⃣ Fetch product
        ProductDto product =
                productClient.getProductById(request.getProductId());

        if (product.getStock() < request.getQuantity()) {
            throw new RuntimeException("Insufficient stock");
        }

        // 3️⃣ Calculate total
        BigDecimal total =
                product.getPrice()
                        .multiply(BigDecimal.valueOf(request.getQuantity()));

        // 4️⃣ Create order
        Order order = new Order();
        order.setUserId(request.getUserId());
        order.setProductId(request.getProductId());
        order.setQuantity(request.getQuantity());
        order.setTotalAmount(total);
        order.setStatus(OrderStatus.CREATED);

        Order saved = repository.save(order);

        return new OrderResponse(
                saved.getId(),
                saved.getUserId(),
                saved.getProductId(),
                saved.getQuantity(),
                saved.getTotalAmount(),
                saved.getStatus()
        );
    }

    @Override
    public OrderResponse getById(Long id) {
        var order = repository.getById(id);
        return new OrderResponse(order.getId(), order.getUserId(), order.getProductId(), order.getQuantity(), order.getTotalAmount(), order.getStatus());
    }
}