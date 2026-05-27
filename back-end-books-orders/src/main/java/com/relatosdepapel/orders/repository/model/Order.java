package com.relatosdepapel.orders.repository.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad JPA que representa una orden de compra.
 * Relaciones:
 * - OneToMany con OrderItem: Una orden puede tener múltiples items (libros).
 * Usa CascadeType.ALL para eliminar automáticamente los items cuando se elimina la orden.
 * Usa Lazy loading para optimizar consultas.
 * Campos de auditoría:
 * - created_at: Timestamp de creación (autogenerado).
 * - updated_at: Timestamp de última actualización (autoactualizado en cada cambio).
 * Restricciones:
 * - status: Debe ser uno de los valores definidos en el enum OrderStatus.
 * - total: Cantidad total de la orden (suma de subtotales de items).
 * - owner_id: Referencia al usuario propietario de la orden.
 */
@Entity
@Table(
        name = "orders",
        indexes = {
                @Index(name = "idx_orders_owner_id", columnList = "owner_id"),
                @Index(name = "idx_orders_status", columnList = "status"),
                @Index(name = "idx_orders_order_date", columnList = "order_date")
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(name = "order_date", nullable = false)
    private LocalDateTime orderDate;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal total;

    @Column(columnDefinition = "TEXT")
    private String comment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private OrderStatus status = OrderStatus.EN_PROCESO;

    @Column(name = "owner_id", nullable = false)
    private Integer ownerId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Items de la orden (libros comprados).
     * Relación OneToMany con CascadeType.ALL: cuando se elimina la orden,
     * se eliminan automáticamente todos sus items.
     * Lazy loading: se carga bajo demanda.
     */
    @OneToMany(
            mappedBy = "order",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();

    /**
     * Método helper para agregar un item a la orden.
     * Mantiene la relación bidireccional consistente.
     *
     * @param orderItem el item a agregar
     */
    public void addItem(OrderItem orderItem) {
        if (items == null) {
            items = new ArrayList<>();
        }
        items.add(orderItem);
        orderItem.setOrder(this);
    }

    /**
     * Método helper para remover un item de la orden.
     * Mantiene la relación bidireccional consistente.
     *
     * @param orderItem el item a remover
     */
    public void removeItem(OrderItem orderItem) {
        if (items != null) {
            items.remove(orderItem);
            orderItem.setOrder(null);
        }
    }
}
