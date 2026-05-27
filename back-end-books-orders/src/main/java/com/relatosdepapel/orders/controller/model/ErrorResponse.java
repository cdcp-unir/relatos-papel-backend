package com.relatosdepapel.orders.controller.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO genérico para respuestas de error.
 * <p>
 * Utilizado por el ControllerAdvice para devolver detalles de errores en formato JSON.
 * <p>
 * Ejemplo:
 * {
 * "details": "Book with ID 42 not found"
 * }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorResponse {

    /**
     * Descripción detallada del error.
     */
    @JsonProperty("details")
    private String details;
}
