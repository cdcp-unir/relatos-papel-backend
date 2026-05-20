package com.relatosdepapel.orders.controller.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO para la respuesta de obtención de órdenes recientes.
 * <p>
 * Contiene una lista de órdenes recientes del usuario.
 * <p>
 * Ejemplo:
 * {
 * "orders": [
 * {
 * "id": 1,
 * "date": "2025-05-20T10:30:00",
 * "status": "EN_PROCESO",
 * "total": 49.99,
 * "comment": "Entrega rápida",
 * "items": [...]
 * }
 * ]
 * }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetOrdersResponseDto {

    /**
     * Lista de órdenes recientes del usuario.
     */
    @JsonProperty("orders")
    private List<RecentOrder> orders;
}
