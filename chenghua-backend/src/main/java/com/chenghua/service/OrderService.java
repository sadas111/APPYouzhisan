package com.chenghua.service;

import com.chenghua.dto.OrderCreateRequest;
import com.chenghua.entity.Address;
import com.chenghua.entity.Order;
import com.chenghua.repository.AddressRepository;
import com.chenghua.repository.OrderRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private ObjectMapper objectMapper;

    public Order createOrder(OrderCreateRequest request) {
        Order order = new Order();
        order.setUserId(request.getUserId());
        order.setTotalAmount(request.getTotalAmount());
        order.setItems(request.getItems());
        
        // Lookup address
        if (request.getAddressId() != null) {
            Address address = addressRepository.findByIdAndUserId(request.getAddressId(), request.getUserId()).orElse(null);
            if (address != null) {
                try {
                    order.setAddressSnapshot(objectMapper.writeValueAsString(address));
                } catch (Exception e) {
                    e.printStackTrace();
                    // Fallback or ignore
                }
            }
        }

        order.setOrderNo(generateOrderNo());
        order.setStatus("PAID");
        return orderRepository.save(order);
    }

    public List<Order> getUserOrders(Long userId) {
        return orderRepository.findByUserIdOrderByCreateTimeDesc(userId);
    }

    private String generateOrderNo() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + 
               UUID.randomUUID().toString().substring(0, 4).toUpperCase();
    }
}
