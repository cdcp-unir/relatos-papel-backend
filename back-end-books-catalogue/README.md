# back-end-books-catalogue

Microservicio de catálogo de la aplicación **Relatos de papel**. Gestiona el CRUD completo de libros

## Tabla de contenidos

- [Arquitectura general](#arquitectura-general)
- [Capa controladora (Controller)](#capa-controladora)
- [Capa de servicio (Service)](#capa-de-servicio)
- [Capa de acceso a datos (Repository)](#capa-de-acceso-a-datos)
- [Modelo relacional de base de datos](#modelo-relacional-de-base-de-datos)
- [Reconstrucción de la base de datos para pruebas](#reconstrucción-de-la-base-de-datos-para-pruebas)
- [Configuración](#configuración)

---

## Arquitectura general

El microservicio sigue una arquitectura en capas clásica de Spring Boot:

```
Controller → Service → Repository → Base de datos MySQL
```

Se registra en **Eureka** como `books-catalogue` y utiliza **Spring Data JPA** con **Hibernate** para el acceso a datos. La validación del esquema se realiza con `ddl-auto: validate`.

---

## Capa controladora

Existen **tres controladores REST**, cada uno expuesto bajo una versión diferente de la API. Todos permiten CORS desde cualquier origen.

### `BooksController` — `/api/v1/`

Controlador principal con operaciones CRUD completas.

| Método   | Endpoint             | Descripción                                               | Request Body             | Response              |
|----------|----------------------|-----------------------------------------------------------|--------------------------|-----------------------|
| `GET`    | `/api/v1/books`      | Lista todos los libros con stock > 0                      | —                        | `GetBooksResponseDto` |
| `GET`    | `/api/v1/books/{id}` | Obtiene un libro por ID (con specs e imágenes)            | —                        | `GetBookResponseDto`  |
| `POST`   | `/api/v1/books`      | Crea un nuevo libro                                       | `WriteSupplyRequestDto`  | `GetBookResponseDto`  |
| `PUT`    | `/api/v1/books/{id}` | Reemplaza completamente un libro                          | `WriteSupplyRequestDto`  | `GetBookResponseDto`  |
| `PATCH`  | `/api/v1/books/{id}` | Actualización parcial vía **JSON Merge Patch** (RFC 7386) | JSON parcial (String)    | `GetBookResponseDto`  |
| `DELETE` | `/api/v1/books/{id}` | Elimina un libro                                          | —                        | `204 No Content`      |

### `BooksControllerGetWithPredicate` — `/api/v2/`

Búsqueda con filtros dinámicos usando **JPA Specifications**.

| Método | Endpoint        | Parámetros opcionales                                              |
|--------|-----------------|--------------------------------------------------------------------|
| `GET`  | `/api/v2/books` | `name`, `description`, `fullDescription`, `type`, `price`, `stock` |

- Los campos de texto (`name`, `description`, `fullDescription`) filtran con **LIKE** (coincidencia parcial, case-insensitive).
- `type` filtra con **igualdad exacta**.
- `price` filtra con **≤** (menor o igual).
- `stock` filtra con **≥** (mayor o igual).

### `BooksControllerGetWithPredicateAndPagination` — `/api/v3/`

Igual que v2 pero con **paginación**.

| Método | Endpoint        | Parámetros adicionales                          |
|--------|-----------------|-------------------------------------------------|
| `GET`  | `/api/v3/books` | `pageSize` (default: 5), `page` (default: 0)    |

### Manejo de errores — `BooksControllerAdvice`

El `@ControllerAdvice` captura `BookNotFoundException` y devuelve un **404 Not Found** con un cuerpo `ErrorResponse`:

```json
{
  "details": "Book not found with id: 42"
}
```

### DTOs

| DTO                   | Uso                                                                    |
|-----------------------|------------------------------------------------------------------------|
| `GetBooksResponseDto` | Lista de libros (vista resumida sin specs ni imágenes)                 |
| `GetBookResponseDto`  | Detalle completo de un libro (con specs e imágenes)                    |
| `BookDto`             | Vista resumida de un libro (id, name, description, type, price, stock) |
| `WriteBookRequestDto` | Cuerpo de creación/actualización (incluye specs e imágenes)            |
| `SpecificationDto`    | Par clave-valor de una especificación (`specKey`, `specValue`)         |
| `ErrorResponse`       | Respuesta de error genérica                                            |

---

## Capa de servicio

Cada operación de negocio está separada en su propio servicio:

### `GetBooksService`

- `getBooks()`: Obtiene todos los libros con stock > 0 usando `findAvailableBooks()` (JPQL).
- `getBook(Integer id)`: Obtiene un libro por ID con detalle completo (especificaciones e imágenes). Lanza `BookNotFoundException` si no existe.

### `GetBooksWithPredicateService`

- `getBooks(name, description, fullDescription, type, price, stock)`: Si se proporciona al menos un filtro, construye una `Specification` dinámica; si no, devuelve todos los disponibles.

### `GetBooksWithPredicateAndPaginationService`

- Igual que el anterior pero delega en `BookRepository.getBooks(...)` con parámetros `pageSize` y `page` para paginación.

### `CreateBooksService`

- `createBook(WriteBookRequestDto)`: Crea la entidad `Book` junto con sus `BookSpecification` e `BookImage` asociadas. Gracias a `CascadeType.ALL`, las entidades hijas se persisten automáticamente. Operación `@Transactional`.

### `ModifyBooksService`

- `modifyBookInteger id, WriteBookRequestDto)`: **PUT** — Reemplaza completamente el libro. Elimina las specs e imágenes antiguas y crea las nuevas.
- `modifyBook(Integer id, String jsonPart)`: **PATCH** — Aplica un **JSON Merge Patch** (RFC 7386) usando la librería `json-patch`. Convierte el libro existente a JSON, aplica el merge patch y persiste el resultado.

### `DeleteBooksService`

- `deleteBook(int id)`: Verifica existencia y elimina. Las specs e imágenes se eliminan por cascada (`ON DELETE CASCADE`). Lanza `BookNotFoundException` si no existe.

### `BookMapper` (Utilidad)

Componente de mapeo entre entidades JPA y DTOs. Gestiona también la eliminación y re-creación de especificaciones e imágenes al actualizar un libro (borra las antiguas con `deleteByBookId` y crea las nuevas).

---

## Capa de acceso a datos

### Repositorios JPA

| Repositorio                  | Entidad             | Hereda de                                                            | Funcionalidad destacada                                |
|------------------------------|---------------------|----------------------------------------------------------------------|--------------------------------------------------------|
| `BookJpaRepository`          | `Book`              | `JpaRepository`, `JpaSpecificationExecutor`, `PagingAndSortingRepository` | Consultas JPQL, nativas, derivadas y Specifications    |
| `SpecificationJpaRepository` | `BookSpecification` | `JpaRepository`                                                      | `findBySupplyId`, `deleteBySupplyId` (query nativa)    |
| `ImageJpaRepository`         | `BookImage`         | `JpaRepository`                                                      | `findBySupplyId`, `deleteBySupplyId` (query nativa)    |

### `BookRepository` (Repositorio compuesto)

Clase `@Repository` que encapsula la lógica de construcción de `Specification` dinámicas y paginación. Métodos principales:

- `getBooks()` — Devuelve libros disponibles (stock > 0).
- `getBooks(size, page)` — Paginación simple sin filtros.
- `getBooks(name, description, ..., stock)` — Búsqueda con filtros dinámicos.
- `getBooks(name, description, ..., stock, pageSize, page)` — Búsqueda con filtros + paginación.

### Consultas disponibles en `BookJpaRepository`

| Método                          | Tipo        | Descripción                                        |
|---------------------------------|-------------|----------------------------------------------------|
| `findAvailableSupplies()`       | JPQL        | Suministros con stock > 0                          |
| `findAvailableSuppliesNative()` | SQL nativa  | Equivalente nativa de la anterior                  |
| `findAllWithDetails()`          | JPQL        | Todos los suministros con JOIN FETCH de specs e imágenes |
| `findAllWithDetailsNative()`    | SQL nativa  | Equivalente nativa con LEFT JOIN                   |
| `findByTypeIgnoreCase(type)`    | Derivada    | Filtro por tipo (case-insensitive)                 |
| `findByNameContainingIgnoreCase(name)` | Derivada | Búsqueda parcial por nombre                  |

### Sistema de predicados dinámicos (JPA Specifications)

El paquete `repository.predicate` implementa un motor de consultas dinámicas:

- **`SearchCriteria<T>`**: Implementa `Specification<T>`, acumula `SearchStatement` y genera predicados JPA en `toPredicate()`.
- **`SearchStatement`**: Tripleta `(key, value, operation)`.
- **`SearchOperation`**: Enum con operaciones: `GREATER_THAN`, `LESS_THAN`, `GREATER_THAN_EQUAL`, `LESS_THAN_EQUAL`, `NOT_EQUAL`, `EQUAL`, `MATCH` (LIKE), `MATCH_END`.
- **`SearchFields`**: Constantes con los nombres de campos de la entidad `Supply`.

---

## Modelo relacional de base de datos

```
┌──────────────────────────┐
│         category         │
├──────────────────────────┤
│ id          INTEGER (PK) │──────┐
│ name        VARCHAR(255) │      │
└──────────────────────────┘      │
        ┌─────────────────────────┼
        │ 1:N             
        ▼                                          
┌────────────────────────┐
│        book            │
├─────────────────────── ┤
│ id, nombre        INTEGER (PK) 
│ book_id   INTEGER (FK) │
│ spec_key  VARCHAR(100) │
│ spec_value VARCHAR(255)│
├────────────────────────┤
│ UK(book  _id, spec_key)│
└────────────────────────┘
```

### Relaciones

- **`book` → `spec`**: Relación **1:N**. Cada libro puede tener múltiples especificaciones. Clave única compuesta `(supply_id, spec_key)` para evitar duplicados. Cascade `ON DELETE CASCADE`.
- **`book` → `image`**: Relación **1:N**. Cada suministro puede tener múltiples imágenes. Cascade `ON DELETE CASCADE`.

### Entidades JPA

| Entidad              | Tabla    | Relaciones                                                  |
|----------------------|----------|-------------------------------------------------------------|
| `Supply`             | `supply` | `@OneToMany` → `SupplySpecification`, `@OneToMany` → `SupplyImage` (Lazy, CascadeType.ALL) |
| `SupplySpecification`| `spec`   | `@ManyToOne` → `Supply` (Lazy)                              |
| `SupplyImage`        | `image`  | `@ManyToOne` → `Supply` (Lazy)                              |

---

## Reconstrucción de la base de datos para pruebas

Los scripts SQL se encuentran en `src/main/resources/db/`. Deben ejecutarse en el siguiente orden sobre una instancia de **MySQL**:

### Paso 1: Crear el esquema y las tablas

```bash
mysql -u root -p < src/main/resources/db/schema.sql
```

Este script:
- Crea el schema `supplies_catalogue`.
- Crea las tablas `supply`, `spec` e `image` con sus claves foráneas y restricciones.

### Paso 2: (Opcional) Crear índices de rendimiento

```bash
mysql -u root -p supplies_catalogue < src/main/resources/db/indices.sql
```

Crea índices en:
- `supply`: `type`, `name`, `stock`, `price`
- `spec`: `supply_id`, `spec_key`
- `image`: `supply_id`

### Paso 3: Insertar datos de ejemplo

```bash
mysql -u root -p supplies_catalogue < src/main/resources/db/ejemplos.sql
```

Inserta **100 suministros de oficina** con:
- 2 especificaciones por suministro (200 registros en `spec`)
- 1 imagen por suministro (100 registros en `image`)

### Script completo (one-liner)

```bash
mysql -u root -p < src/main/resources/db/schema.sql && \
mysql -u root -p supplies_catalogue < src/main/resources/db/indices.sql && \
mysql -u root -p supplies_catalogue < src/main/resources/db/ejemplos.sql
```

### Reconstrucción desde cero (drop + create)

Si la base de datos ya existe y quieres reconstruirla:

```bash
mysql -u root -p -e "DROP SCHEMA IF EXISTS supplies_catalogue;" && \
mysql -u root -p < src/main/resources/db/schema.sql && \
mysql -u root -p supplies_catalogue < src/main/resources/db/indices.sql && \
mysql -u root -p supplies_catalogue < src/main/resources/db/ejemplos.sql
```

---

## Configuración

Variables de entorno configurables (`application.yml`):

| Variable      | Valor por defecto                              | Descripción                  |
|---------------|------------------------------------------------|------------------------------|
| `DB_URL`      | `jdbc:mysql://localhost:3306/supplies_catalogue`| URL de conexión JDBC         |
| `DB_DRIVER`   | `com.mysql.cj.jdbc.Driver`                     | Driver JDBC                  |
| `DB_USER`     | `root`                                         | Usuario de base de datos     |
| `DB_PASSWORD` | `mysql`                                        | Contraseña de base de datos  |
| `EUREKA_URL`  | `http://localhost:8761/eureka`                 | URL del servidor Eureka      |

El servicio se registra en Eureka con el nombre de instancia `unir-supplies-catalogue`.