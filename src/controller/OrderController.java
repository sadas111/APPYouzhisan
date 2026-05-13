package com.chenghua.controller;

import com.chenghua.dto.OrderCreateRequest;
import com.chenghua.entity.Order;
import com.chenghua.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping
    public Order createOrder(@RequestBody OrderCreateRequest request) {
        if (request.getUserId() == null) {
            request.setUserId(1L);
        }
        return orderService.createOrder(request);
    }

    @GetMapping
    public List<Order> getUserOrders(@RequestParam(required = false) Long userId) {
        Long finalUserId = (userId != null) ? userId : 1L;
        return orderService.getUserOrders(finalUserId);
    }
}
