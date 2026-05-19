-- =========================================================
-- Relatos de Papel - Catalogue Service
-- DDL script
-- Base de datos: catalogue_db
-- =========================================================

CREATE DATABASE catalogue_db;

-- Conectarse manualmente a catalogue_db antes de ejecutar lo siguiente.
-- En pgAdmin: selecciona catalogue_db y ejecuta desde ahí.
-- En psql: \c catalogue_db

CREATE TABLE IF NOT EXISTS books (
    id BIGSERIAL PRIMARY KEY,

    external_id UUID NOT NULL UNIQUE,

    title VARCHAR(200) NOT NULL,
    author VARCHAR(150) NOT NULL,
    publication_date DATE,
    category VARCHAR(100) NOT NULL,

    isbn VARCHAR(20) NOT NULL UNIQUE,

    rating INTEGER NOT NULL,
    visible BOOLEAN NOT NULL DEFAULT TRUE,
    stock INTEGER NOT NULL DEFAULT 0,

    price NUMERIC(10, 2) NOT NULL DEFAULT 0.00,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_books_rating
        CHECK (rating BETWEEN 1 AND 5),

    CONSTRAINT chk_books_stock
        CHECK (stock >= 0),

    CONSTRAINT chk_books_price
        CHECK (price >= 0)
);

CREATE INDEX IF NOT EXISTS idx_books_title
    ON books (title);

CREATE INDEX IF NOT EXISTS idx_books_author
    ON books (author);

CREATE INDEX IF NOT EXISTS idx_books_category
    ON books (category);

CREATE INDEX IF NOT EXISTS idx_books_publication_date
    ON books (publication_date);

CREATE INDEX IF NOT EXISTS idx_books_rating
    ON books (rating);

CREATE INDEX IF NOT EXISTS idx_books_visible
    ON books (visible);