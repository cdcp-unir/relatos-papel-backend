package com.relatosdepapel.orders.controller.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.relatosdepapel.orders.repository.model.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO que representa el detalle de una orden reciente.
 * <p>
 * Contiene la información principal de una orden y sus ítems comprados.
 * <p>
 * Ejemplo:
 * {
 * "id": 1,
 * "date": "2025-05-20T10:30:00",
 * "status": "EN_PROCESO",
 * "total": 49.99,
 * "comment": "Entrega rápida",
 * "items": [
 * {
 * "name": "Harry Potter",
 * "quantity": 2,
 * "price": 19.99
 * }
 * ]
 * }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecentOrder {

    /**
     * ID único de la orden.
     */
    @JsonProperty("id")
    private Integer id;

    /**
     * Fecha de creación de la orden.
     */
    @JsonProperty("date")
    private LocalDateTime date;

    /**
     * Estado actual de la orden: EN_PROCESO, CANCELADO, ENTREGADO.
     */
    @JsonProperty("status")
    private OrderStatus status;

    /**
     * Total de la orden (suma de subtotales de items).
     */
    @JsonProperty("total")
    private BigDecimal total;

    /**
     * Comentario o nota asociada a la orden (opcional).
     */
    @JsonProperty("comment")
    private String comment;

    /**
     * Lista de ítems comprados en esta orden.
     */
    @JsonProperty("items")
    private List<PurchasedItem> items;
}
