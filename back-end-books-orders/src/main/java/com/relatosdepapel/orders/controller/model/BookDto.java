package com.relatosdepapel.orders.controller.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO simplificado que representa un libro recibido del microservicio de catálogo.
 * <p>
 * Modelo utilizado por la capa Facade (BooksCatalogueFacade) para mapear
 * las respuestas HTTP del microservicio books-catalogue.
 * <p>
 * Ejemplo:
 * {
 * "id": 1,
 * "name": "Harry Potter and the Sorcerer's Stone",
 * "description": "A magical adventure",
 * "price": 19.99,
 * "stock": 150
 * }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookDto {

    /**
     * ID único del libro en el catálogo.
     */
    @JsonProperty("id")
    private Integer id;

    /**
     * Nombre del libro.
     */
    @JsonProperty("name")
    private String name;

    /**
     * Descripción breve del libro.
     */
    @JsonProperty("description")
    private String description;

    /**
     * Precio unitario del libro.
     */
    @JsonProperty("price")
    private BigDecimal price;

    /**
     * Stock disponible del libro.
     */
    @JsonProperty("stock")
    private Integer stock;
}
