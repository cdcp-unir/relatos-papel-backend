package com.relatosdepapel.orders.controller.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO que representa un ítem (libro) comprado en una orden.
 * <p>
 * Contiene la información básica del libro comprado: nombre, cantidad y precio actual.
 * <p>
 * Ejemplo:
 * {
 * "name": "Harry Potter",
 * "quantity": 2,
 * "price": 19.99
 * }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchasedItem {

    /**
     * Nombre del libro comprado.
     */
    @JsonProperty("name")
    private String name;

    /**
     * Cantidad de libros comprados en este ítem.
     */
    @JsonProperty("quantity")
    private Integer quantity;

    /**
     * Precio unitario del libro en el momento de la compra.
     */
    @JsonProperty("price")
    private BigDecimal price;
}
