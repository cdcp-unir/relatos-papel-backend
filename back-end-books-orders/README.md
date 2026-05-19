# back-end-supplies-orders

Microservicio de pedidos de la aplicación **UNIR Supplies**. Gestiona la creación y consulta de órdenes de compra, comunicándose con el microservicio de catálogo para validar productos y actualizar stock.

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
Controller → Service → Facade (WebClient) → supplies-catalogue
                     → Repository → Base de datos MySQL
```

Se registra en **Eureka** como `supplies-orders` y utiliza **WebClient** con `@LoadBalanced` para resolver las URLs de otros microservicios vía Service Discovery. Escucha en el **puerto 8081**.

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
| `SupplyNotFoundException`       | `404 Not Found`          | El producto solicitado no existe en el catálogo     |
| `BadSupplyModificationException`| `400 Bad Request`        | Error al intentar modificar el stock del producto   |
| `InternalErrorException`        | `500 Internal Server Error` | Error interno al comunicarse con el catálogo     |

Respuesta de error:
```json
{
  "details": "Supply with ID 42 not found"
}
```

### DTOs

| DTO                        | Uso                                                              |
|----------------------------|------------------------------------------------------------------|
| `CreateOrderRequestDto`    | Cuerpo de creación de orden: lista de `RequestedSupply`          |
| `RequestedSupply`          | Producto solicitado: `id` (del catálogo) y `quantity`            |
| `CreateOrderResponseDto`   | Respuesta de creación: `name` (identificador de la orden)        |
| `GetOrdersResponseDto`     | Lista de `RecentOrder` (órdenes recientes)                       |
| `RecentOrder`              | Detalle de orden: `id`, `date`, `status`, `total`, `comment`, `items` |
| `PurchasedItem`            | Ítem comprado: `name`, `quantity`, `price`                       |
| `ErrorResponse`            | Respuesta de error genérica                                      |

---

## Capa de servicio

### `CreateOrdersService`

Orquesta el flujo completo de creación de una orden (`@Transactional`):

1. **Valida** que la solicitud contenga al menos un producto.
2. **Para cada producto solicitado**:
   - Valida que la cantidad sea > 0.
   - Consulta el catálogo vía `SuppliesCatalogueFacade.getSupply()` para obtener precio y stock actual.
   - Verifica que haya stock suficiente.
   - Calcula el subtotal (`precio × cantidad`).
3. **Genera** un nombre de orden único (`ORDER-{timestamp}`).
4. **Persiste** la orden con sus ítems (cascade).
5. **Actualiza el stock** de cada producto en el catálogo vía `SuppliesCatalogueFacade.updateSupplyStock()` (PATCH).
6. **Retorna** el nombre de la orden creada.

> **Nota**: El `ownerId` está hardcodeado a `1`. Debería obtenerse del contexto de seguridad.

### `GetOrdersService`

- `getRecentOrders()`: Obtiene las **5 órdenes más recientes** del usuario (ordenadas por fecha descendente). Para cada ítem de la orden, consulta al catálogo para obtener el nombre y precio actual del producto.

---

## Facade — Comunicación entre microservicios

### `SuppliesCatalogueFacade`

Componente que encapsula las llamadas HTTP al microservicio **supplies-catalogue** usando `WebClient` (reactivo, con `@LoadBalanced` para service discovery):

| Método                                  | HTTP        | Endpoint del catálogo              | Descripción                              |
|-----------------------------------------|-------------|------------------------------------|------------------------------------------|
| `getSupply(Integer supplyId)`           | `GET`       | `/api/v1/supplies/{id}`            | Obtiene datos de un producto             |
| `updateSupplyStock(Integer id, Integer stock)` | `PATCH` | `/api/v1/supplies/{id}`       | Actualiza el stock (JSON Merge Patch)    |

Manejo de errores HTTP:
- **404** → `SupplyNotFoundException`
- **400** → `BadSupplyModificationException`
- **500** → `InternalErrorException`

### `SupplyDto` (Facade Model)

Modelo simplificado del producto recibido del catálogo: `id`, `name`, `description`, `price`, `stock`.

### Configuración del WebClient

```java
@LoadBalanced
@Bean
public WebClient.Builder webClient() { ... }
```

La anotación `@LoadBalanced` permite resolver el nombre del servicio (`supplies-catalogue`) registrado en Eureka en lugar de usar URLs hardcodeadas.

---

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
| `OrderItem` | `order_item`  | `id`, `order` (FK), `idCatalogue`, `quantity`, `subTotal`                   |

### `OrderStatus` (Enum)

```java
EN_PROCESO, CANCELADO, ENTREGADO
```

### Relaciones entre entidades

- **`Order` → `OrderItem`**: Relación **1:N** con `CascadeType.ALL` y `orphanRemoval = true`. Los ítems se persisten y eliminan automáticamente con la orden.
- **`OrderItem` → `Order`**: `@ManyToOne` (Lazy).
- **`OrderItem.idCatalogue`**: Referencia lógica (no FK) al ID del producto en el microservicio de catálogo.

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
│ comment     TEXT              │      │
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
                    │        order_item             │
                    ├──────────────────────────────┤
                    │ id           INTEGER (PK)     │
                    │ order_id     INTEGER (FK)      │
                    │ id_catalogue INTEGER           │  ← Ref. lógica a supplies-catalogue
                    │ quantity     INTEGER (≥ 0)     │
                    │ sub_total    DECIMAL(10,2)     │
                    └──────────────────────────────┘
```

### Relaciones

- **`orders` → `order_item`**: Relación **1:N**. Cascade `ON DELETE CASCADE`. Cada orden puede tener múltiples ítems.
- **`order_item.id_catalogue`**: Referencia lógica al `supply.id` del microservicio de catálogo (no hay FK física, ya que están en bases de datos distintas).

---

## Reconstrucción de la base de datos para pruebas

El script SQL se encuentra en `src/main/resources/db/schema.sql`.

### Paso 1: Crear el esquema y las tablas

```bash
mysql -u root -p < src/main/resources/db/schema.sql
```

Este script:
- Crea el schema `supplies_orders`.
- Crea la tabla `orders` con campo `status` de tipo ENUM.
- Crea la tabla `order_item` con FK a `orders` y constraint CHECK en `quantity`.

### Reconstrucción desde cero

```bash
mysql -u root -p -e "DROP SCHEMA IF EXISTS supplies_orders;" && \
mysql -u root -p < src/main/resources/db/schema.sql
```

> **Nota**: Este microservicio no incluye script de datos de ejemplo (`ejemplos.sql`). Las órdenes se crean mediante la API REST.

---

## Configuración

Variables de entorno configurables (`application.yml`):

| Variable                  | Valor por defecto                                | Descripción                                  |
|---------------------------|--------------------------------------------------|----------------------------------------------|
| `DB_URL`                  | `jdbc:mysql://localhost:3307/supplies_orders`    | URL de conexión JDBC (puerto 3307)           |
| `DB_DRIVER`               | `com.mysql.cj.jdbc.Driver`                       | Driver JDBC                                  |
| `DB_USER`                 | `root`                                           | Usuario de base de datos                     |
| `DB_PASSWORD`             | `mysql`                                          | Contraseña de base de datos                  |
| `EUREKA_URL`              | `http://localhost:8761/eureka`                   | URL del servidor Eureka                      |
| `SUPPLIES_CATALOGUE_URL`  | `http://supplies-catalogue/api/v1`               | URL base del microservicio de catálogo (resuelta vía Eureka) |