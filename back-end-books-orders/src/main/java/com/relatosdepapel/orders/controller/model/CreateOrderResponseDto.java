package com.relatosdepapel.orders.controller.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para la respuesta de creación de una orden.
 * <p>
 * Contiene el identificador único (nombre) de la orden creada.
 * <p>
 * Ejemplo:
 * {
 * "name": "ORDER-1716178800123"
 * }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateOrderResponseDto {

    /**
     * Nombre/identificador único de la orden creada.
     * Formato: ORDER-{timestamp}
     */
    @JsonProperty("name")
    private String name;
}
