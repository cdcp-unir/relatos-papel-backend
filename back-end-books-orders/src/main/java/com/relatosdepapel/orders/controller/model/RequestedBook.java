package com.relatosdepapel.orders.controller.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO que representa un libro solicitado en una orden de compra.
 * <p>
 * Contiene el ID del libro (referencia al catálogo) y la cantidad solicitada.
 * <p>
 * Ejemplo:
 * {
 * "id": 1,
 * "quantity": 2
 * }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequestedBook {

    /**
     * ID del libro en el microservicio de catálogo.
     */
    @JsonProperty("id")
    private Integer id;

    /**
     * Cantidad de libros solicitados.
     * Debe ser mayor a 0.
     */
    @JsonProperty("quantity")
    private Integer quantity;
}
