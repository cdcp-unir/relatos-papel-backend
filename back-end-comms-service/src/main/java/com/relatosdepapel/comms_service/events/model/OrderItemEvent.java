package com.relatosdepapel.comms_service.events.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemEvent {
    private Integer id;
    private Integer quantity;
    private BigDecimal subTotal;
}