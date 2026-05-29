-- =========================================================
-- Relatos de Papel - Orders Service
-- DDL script
-- Base de datos: books_orders
-- =========================================================

CREATE DATABASE orders_db;

\connect orders_db;

-- ===============================================
-- Tabla: orders
-- ===============================================
CREATE TABLE IF NOT EXISTS orders (
                                      id         SERIAL PRIMARY KEY,
                                      name       VARCHAR(255)   NOT NULL,
                                      order_date TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                      total      NUMERIC(10, 2) NOT NULL DEFAULT 0.00,
                                      comment    TEXT,
                                      status     VARCHAR(20)    NOT NULL DEFAULT 'EN_PROCESO' CHECK (
                                          status IN (
                                                     'EN_PROCESO',
                                                     'CANCELADO',
                                                     'ENTREGADO'
                                              )
                                          ),
                                      owner_id   INT            NOT NULL,
                                      created_at TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                      updated_at TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Índices (se crean fuera de la definición de tabla en PostgreSQL)
CREATE INDEX idx_orders_status ON orders (status);

CREATE INDEX idx_orders_order_date ON orders (order_date);

-- Trigger para actualizar automáticamente updated_at
CREATE OR REPLACE FUNCTION update_timestamp()
    RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_update_orders_timestamp
    BEFORE UPDATE ON orders
    FOR EACH ROW
EXECUTE FUNCTION update_timestamp();

-- ===============================================
-- Tabla: order_item
-- ===============================================
CREATE TABLE IF NOT EXISTS order_item (
                                          id        SERIAL PRIMARY KEY,
                                          order_id  INT            NOT NULL,
                                          book_id   UUID           NOT NULL,
                                          quantity  INT            NOT NULL CHECK (quantity >= 0),
                                          sub_total NUMERIC(10, 2) NOT NULL,
                                          CONSTRAINT fk_order_item_order FOREIGN KEY (order_id) REFERENCES orders (id) ON DELETE CASCADE ON UPDATE CASCADE
);

-- Índices
CREATE INDEX idx_order_item_order_id ON order_item (order_id);

CREATE INDEX idx_order_item_book_id ON order_item (book_id);