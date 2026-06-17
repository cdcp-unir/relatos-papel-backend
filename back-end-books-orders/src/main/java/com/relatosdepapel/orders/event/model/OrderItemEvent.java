package com.relatosdepapel.orders.event.model;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Builder
@Data
public class OrderItemEvent {
    private Integer id;
    private Integer quantity;
    private BigDecimal subTotal;
}