package com.relatosdepapel.orders.controller.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO para la solicitud de creación de una nueva orden de compra.
 * <p>
 * Contiene una lista de libros solicitados con sus cantidades.
 * Debe contener al menos un libro.
 * <p>
 * Ejemplo:
 * {
 * "items": [
 * { "id": 1, "quantity": 2 },
 * { "id": 5, "quantity": 1 }
 * ]
 * }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateOrderRequestDto {

    /**
     * Lista de libros solicitados en la orden.
     * Cada elemento contiene el ID del libro y la cantidad deseada.
     */
    @JsonProperty("items")
    private List<RequestedBook> items;
}
