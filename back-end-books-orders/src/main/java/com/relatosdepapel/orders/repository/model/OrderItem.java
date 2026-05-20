package com.relatosdepapel.orders.repository.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Entidad JPA que representa un item (línea) dentro de una orden de compra.
 * Cada OrderItem representa un libro específico con su cantidad y subtotal.
 * <p>
 * Relaciones:
 * - ManyToOne con Order: Múltiples items pertenecen a una sola orden.
 * Usa Lazy loading para optimizar consultas.
 * <p>
 * Restricciones:
 * - quantity: Cantidad de libros (>= 0).
 * - sub_total: Subtotal de esta línea de orden.
 * - book_id: Referencia al libro en el microservicio de catálogo (no es FK en esta BD).
 */
@Entity
@Table(
        name = "order_item",
        indexes = {
                @Index(name = "idx_order_item_order_id", columnList = "order_id"),
                @Index(name = "idx_order_item_book_id", columnList = "book_id")
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * Referencia a la orden a la que pertenece este item.
     * Relación ManyToOne (muchos items pertenecen a una orden).
     * Lazy loading: se carga bajo demanda.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "order_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_order_item_order")
    )
    private Order order;

    /**
     * Referencia al libro (ID del catálogo).
     * No es una relación FK en esta BD, es una referencia al microservicio de catálogo.
     */
    @Column(name = "book_id", nullable = false)
    private Integer bookId;

    /**
     * Cantidad de libros en este item de la orden.
     * Restricción: debe ser >= 0.
     */
    @Column(nullable = false)
    private Integer quantity;

    /**
     * Subtotal para este item (price * quantity).
     * Precisión: 10 dígitos, 2 decimales.
     */
    @Column(name = "sub_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal subTotal;
}
