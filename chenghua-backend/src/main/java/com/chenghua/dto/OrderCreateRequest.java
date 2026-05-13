package com.chenghua.dto;

import com.chenghua.entity.OrderItem;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
public class OrderCreateRequest {
    private Long userId;
    private Long addressId;
    private java.math.BigDecimal totalAmount;
    private java.util.List<OrderItem> items;
}
