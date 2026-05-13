package com.chenghua.controller;

import com.chenghua.dto.OrderCreateRequest;
import com.chenghua.entity.Order;
import com.chenghua.security.AuthenticatedUserService;
import com.chenghua.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private AuthenticatedUserService authenticatedUserService;

    @PostMapping
    public Order createOrder(@RequestBody OrderCreateRequest request) {
        Long currentUserId = authenticatedUserService.requireCurrentUserId();
        request.setUserId(currentUserId);
        return orderService.createOrder(request);
    }

    @GetMapping
    public List<Order> getUserOrders(@RequestParam(required = false) Long userId) {
        Long currentUserId = authenticatedUserService.requireCurrentUserId();

        if (userId != null && !authenticatedUserService.isAdmin() && !userId.equals(currentUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Forbidden");
        }

        Long finalUserId = (userId != null && authenticatedUserService.isAdmin()) ? userId : currentUserId;
        return orderService.getUserOrders(finalUserId);
    }
}
