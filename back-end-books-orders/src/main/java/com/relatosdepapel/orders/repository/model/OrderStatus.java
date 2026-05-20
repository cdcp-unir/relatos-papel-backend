package com.relatosdepapel.orders.repository.model;

/**
 * Enum que representa los posibles estados de una orden de compra.
 * <p>
 * Estados:
 * - EN_PROCESO: Orden creada y en espera de procesamiento o pago.
 * - CANCELADO: Orden cancelada por el cliente o por el sistema.
 * - ENTREGADO: Orden completada y entregada al cliente.
 */
public enum OrderStatus {
    EN_PROCESO("En Proceso"),
    CANCELADO("Cancelado"),
    ENTREGADO("Entregado");

    private final String displayName;

    OrderStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
