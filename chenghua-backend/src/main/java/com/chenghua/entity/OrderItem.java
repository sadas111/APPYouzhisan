package com.chenghua.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // No @ManyToOne back to Order to avoid infinite recursion in simple JSON serialization,
    // or handle with @JsonIgnore. For simplicity, we just use uni-directional from Order.
    // Actually, JPA requires owning side for bidirectional.
    // Let's keep it simple: Order owns OrderItem via @JoinColumn.

    private Long productId;

    private String productName;

    private String productImage;

    private BigDecimal price;

    private Integer quantity;
}
