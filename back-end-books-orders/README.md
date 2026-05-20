# back-end-books-orders

Microservicio de pedidos de la aplicación **Relatos de papel**. Pieza principal que ejecuta el registro de compras de libros y consulta de órdenes de compra de un usuario. Consultará vía peticiones HTTP al microservicio de catalogue para consultar el estado, stock y visibilidad del libro.

## Tabla de contenidos

- [Arquitectura general](#arquitectura-general)
- [Capa controladora (Controller)](#capa-controladora)
- [Capa de servicio (Service)](#capa-de-servicio)
- [Facade — Comunicación entre microservicios](#facade--comunicación-entre-microservicios)
- [Capa de acceso a datos (Repository)](#capa-de-acceso-a-datos)
- [Modelo relacional de base de datos](#modelo-relacional-de-base-de-datos)
- [Reconstrucción de la base de datos para pruebas](#reconstrucción-de-la-base-de-datos-para-pruebas)
- [Configuración](#configuración)

---

## Arquitectura general

El microservicio sigue una arquitectura en capas con un patrón **Facade** para la comunicación inter-servicio:

```
Controller → Service → Facade (WebClient) → books-catalogue → Repository → Base de datos Postgres
```

Se registra en **Eureka** como `books-orders` y utiliza **WebClient** con `@LoadBalanced` para resolver las URLs de otros microservicios vía Service Discovery. Escucha en el **puerto 8081**.

---

## Capa controladora

### `OrdersController` — `/api/v1/`

| Método | Endpoint           | Descripción                                       | Request Body             | Response                  | HTTP Status |
|--------|--------------------|---------------------------------------------------|--------------------------|---------------------------|-------------|
| `GET`  | `/api/v1/orders`   | Obtiene las 5 órdenes más recientes del usuario   | —                        | `GetOrdersResponseDto`    | `200 OK`    |
| `POST` | `/api/v1/orders`   | Crea una nueva orden de compra                    | `CreateOrderRequestDto`  | `CreateOrderResponseDto`  | `201 Created` |

### Manejo de errores — `OrdersControllerAdvice`

| Excepción                       | HTTP Status              | Descripción                                        |
|---------------------------------|--------------------------|----------------------------------------------------|
| `BookNotFoundException`       | `404 Not Found`          | El libro solicitado no existe en el catálogo     |
| `BadBookModificationException`| `400 Bad Request`        | Error al intentar modificar el stock del libro   |
| `InternalErrorException`        | `500 Internal Server Error` | Error interno al comunicarse con el catálogo     |

Respuesta de error:
```json
{
  "details": "Book with ID 42 not found"
}
```

### DTOs

| DTO                        | Uso                                                              |
|----------------------------|------------------------------------------------------------------|
| `CreateOrderRequestDto`    | Cuerpo de creación de orden: lista de `RequestedBook`          |
| `RequestedBook`          | Libro solicitado: `id` (del catálogo) y `quantity`            |
| `CreateOrderResponseDto`   | Respuesta de creación: `name` (identificador de la orden)        |
| `GetOrdersResponseDto`     | Lista de `RecentOrder` (órdenes recientes)                       |
| `RecentOrder`              | Detalle de orden: `id`, `date`, `status`, `total`, `comment`, `items` |
| `PurchasedItem`            | Ítem comprado: `name`, `quantity`, `price`                       |
| `ErrorResponse`            | Respuesta de error genérica                                      |

---

## Capa de servicio

### `CreateOrdersService`

Orquesta el flujo completo de creación de una orden (`@Transactional`):

1. **Valida** que la solicitud contenga al menos un libro.
2. **Para cada libro solicitado**:
   - Valida que la cantidad sea > 0.
   - Consulta el catálogo vía `BooksCatalogueFacade.getBook()` para obtener precio y stock actual.
   - Verifica que haya stock suficiente.
   - Calcula el subtotal (`precio × cantidad`).
3. **Genera** un nombre de orden único (`ORDER-{timestamp}`).
4. **Persiste** la orden con sus ítems (cascade).
5. **Actualiza el stock** de cada libro en el catálogo vía `BooksCatalogueFacade.updateBookStock()` (PATCH).
6. **Retorna** el nombre de la orden creada.

> **Nota**: El `ownerId` está hardcodeado a `1`. Debería obtenerse del contexto de seguridad.

### `GetOrdersService`

- `getRecentOrders()`: Obtiene las **5 órdenes más recientes** del usuario (ordenadas por fecha descendente). Para cada ítem de la orden, consulta al catálogo para obtener el nombre y precio actual del libro.

---

## Facade — Comunicación entre microservicios

### `BooksCatalogueFacade`

Componente que encapsula las llamadas HTTP al microservicio **books-catalogue** usando `WebClient` (reactivo, con `@LoadBalanced` para service discovery):

| Método                                  | HTTP        | Endpoint del catálogo              | Descripción                              |
|-----------------------------------------|-------------|------------------------------------|------------------------------------------|
| `getBook(Integer bookId)`           | `GET`       | `/api/v1/books/{id}`            | Obtiene datos de un libro             |
| `updateBookStock(Integer id, Integer stock)` | `PATCH` | `/api/v1/books/{id}`       | Actualiza el stock (JSON Merge Patch)    |

Manejo de errores HTTP:
- **404** → `BookNotFoundException`
- **400** → `BadBookModificationException`
- **500** → `InternalErrorException`

### `BookDto` (Facade Model)

Modelo simplificado del libro recibido del catálogo: `id`, `name`, `description`, `price`, `stock`.

## Capa de acceso a datos

### `OrderJpaRepository`

| Método                                                   | Tipo      | Descripción                                                 |
|----------------------------------------------------------|-----------|-------------------------------------------------------------|
| `findByOwnerIdOrderByOrderDateDesc(Integer, Limit)`     | Derivada  | Órdenes de un usuario, ordenadas por fecha desc, con límite |
| `findByStatus(OrderStatus)`                              | Derivada  | Órdenes filtradas por estado                                |

Hereda de `JpaRepository<Order, Integer>`.

### Entidades JPA

| Entidad     | Tabla         | Campos principales                                                          |
|-------------|---------------|-----------------------------------------------------------------------------|
| `Order`     | `orders`      | `id`, `name`, `orderDate`, `total`, `comment`, `status` (enum), `ownerId`  |
| `OrderItem` | `order_item`  | `id`, `order` (FK), `book_id`, `quantity`, `subTotal`                   |

### `OrderStatus` (Enum)

```md
EN_PROCESO, CANCELADO, ENTREGADO
```

### Relaciones entre entidades

- **`Order` → `OrderItem`**: Relación **1:N** con `CascadeType.ALL` y `orphanRemoval = true`. Los ítems se persisten y eliminan automáticamente con la orden.
- **`OrderItem` → `Order`**: `@ManyToOne` (Lazy).
- **`OrderItem.bookId`**: Referencia lógica (no FK) al ID del libro en el microservicio de catálogo.

---

## Modelo relacional de base de datos

```
┌──────────────────────────────┐
│           orders             │
├──────────────────────────────┤
│ id          INTEGER (PK)     │──────┐
│ name        VARCHAR(255)     │      │
│ order_date  TIMESTAMP        │      │
│ total       DECIMAL(10,2)    │      │
│ comment     TEXT             │      │
│ status      ENUM             │      │
│   (EN_PROCESO|CANCELADO|     │      │
│    ENTREGADO)                │      │
│ owner_id    INTEGER          │      │
│ created_at  TIMESTAMP        │      │
│ updated_at  TIMESTAMP        │      │
└──────────────────────────────┘      │
                                      │
                                 1:N  │
                                      ▼
                    ┌──────────────────────────────┐
                    │        order_item            │
                    ├──────────────────────────────┤
                    │ id           INTEGER (PK)    │
                    │ order_id     INTEGER (FK)    │
                    │ book_id      INTEGER         │  ← Referencia al id de libro comprado en books-catalogue
                    │ quantity     INTEGER (≥ 0)   │
                    │ sub_total    DECIMAL(10,2)   │
                    └──────────────────────────────┘
```

### Relaciones

- **`orders` → `order_item`**: Relación **1:N**. Cascade `ON DELETE CASCADE`. Cada orden puede tener múltiples ítems.
- **`order_item.book_id`**: Referencia lógica al `book.id` del microservicio de catálogo.

---

## Reconstrucción de la base de datos para pruebas

El script SQL se encuentra en `src/main/resources/db/01_orders_ddl.sql`.

## Configuración

Variables de entorno configurables (`application.yml`):

| Variable                  | Valor por defecto                             | Descripción                                                  |
|---------------------------|-----------------------------------------------|--------------------------------------------------------------|
| `DB_URL`                  | `jdbc:postgres://localhost:5432/books_orders` | URL de conexión JDBC (puerto 3307)                           |
| `DB_DRIVER`               | `org.postgresql.Driver`                       | Driver JDBC                                                  |
| `DB_USER`                 | `root`                                        | Usuario de base de datos                                     |
| `DB_PASSWORD`             | `password`                                    | Contraseña de base de datos                                  |
| `EUREKA_URL`              | `http://localhost:8761/eureka`                | URL del servidor Eureka                                      |
| `BOOK_CATALOGUE_URL`      | `http://books-catalogue/api/v1`               | URL base del microservicio de catálogo (resuelta vía Eureka) |