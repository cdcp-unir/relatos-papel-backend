# back-end-books-catalogue

Microservicio de catálogo para la aplicación **Relatos de Papel**.

Este servicio gestiona el catálogo de libros de la plataforma. Permite crear, consultar, actualizar, eliminar y buscar libros por distintos atributos como título, autor, fecha de publicación, categoría, ISBN, valoración y visibilidad.

Forma parte de la arquitectura de microservicios del proyecto y está preparado para registrarse en **Eureka Server** para que pueda ser consumido por otros servicios, como `orders-service`, y por el `cloud-gateway`.

---

## Tabla de contenidos

- [Tecnologías utilizadas](#tecnologías-utilizadas)
- [Arquitectura del microservicio](#arquitectura-del-microservicio)
- [Estructura del proyecto](#estructura-del-proyecto)
- [Modelo de datos](#modelo-de-datos)
- [Scripts SQL](#scripts-sql)
- [Configuración del proyecto](#configuración-del-proyecto)
- [Cómo iniciar el proyecto](#cómo-iniciar-el-proyecto)
- [Endpoints disponibles](#endpoints-disponibles)
- [Ejemplos de uso](#ejemplos-de-uso)
- [Manejo de errores](#manejo-de-errores)
- [Eureka Client](#eureka-client)
- [Comandos útiles](#comandos-útiles)
- [Notas de entrega](#notas-de-entrega)

---

## Tecnologías utilizadas

- Java 17
- Spring Boot 4.0.6
- Spring Web
- Spring Data JPA
- Hibernate
- Jakarta Validation
- PostgreSQL
- Maven
- Spring Cloud Netflix Eureka Client
- Spring Boot DevTools

---

## Arquitectura del microservicio

El microservicio sigue una arquitectura por capas:

```txt
Controller -> Service -> Repository -> Base de datos PostgreSQL
```

| Capa | Responsabilidad |
|---|---|
| `controller` | Expone los endpoints REST del catálogo |
| `service` | Contiene la lógica de negocio |
| `repository` | Acceso a datos mediante Spring Data JPA |
| `entity` | Define la entidad persistente `Book` |
| `dto` | Define objetos de entrada y salida de la API |
| `exception` | Manejo centralizado de errores |

---

## Estructura del proyecto

```txt
back-end-books-catalogue/
│
├── src/
│   └── main/
│       ├── java/
│       │   └── com/relatosdepapel/catalogueservice/
│       │       ├── CatalogueServiceApplication.java
│       │       ├── controller/
│       │       │   └── BookController.java
│       │       ├── dto/
│       │       │   ├── BookRequest.java
│       │       │   ├── BookResponse.java
│       │       │   └── PaginatedResponse.java
│       │       ├── entity/
│       │       │   └── Book.java
│       │       ├── exception/
│       │       │   ├── ApiError.java
│       │       │   ├── BadRequestException.java
│       │       │   ├── GlobalExceptionHandler.java
│       │       │   └── ResourceNotFoundException.java
│       │       ├── repository/
│       │       │   ├── BookRepository.java
│       │       │   └── BookSpecification.java
│       │       └── service/
│       │           └── BookService.java
│       │
│       └── resources/
│           └── application.yml
│
├── pom.xml
├── mvnw
├── mvnw.cmd
└── README.md
```

Los scripts de base de datos se encuentran en la raíz del repositorio general:

```txt
relatos-papel-backend/
└── database/
    └── catalogue/
        ├── 01_catalogue_ddl.sql
        └── 02_catalogue_dml.sql
```

---

## Modelo de datos

El microservicio utiliza una única tabla principal: `books`.

| Campo | Tipo | Descripción |
|---|---|---|
| `id` | `BIGSERIAL` | Identificador interno de base de datos |
| `external_id` | `UUID` | Identificador público usado por la API |
| `title` | `VARCHAR(200)` | Título del libro |
| `author` | `VARCHAR(150)` | Autor del libro |
| `publication_date` | `DATE` | Fecha de publicación |
| `category` | `VARCHAR(100)` | Categoría del libro |
| `isbn` | `VARCHAR(20)` | Código ISBN único |
| `rating` | `INTEGER` | Valoración entre 1 y 5 |
| `visible` | `BOOLEAN` | Indica si el libro debe mostrarse al cliente |
| `stock` | `INTEGER` | Cantidad disponible |
| `price` | `NUMERIC(10,2)` | Precio del libro |
| `created_at` | `TIMESTAMP` | Fecha de creación del registro |

### Reglas principales

- `external_id` es único y se utiliza como identificador público.
- `isbn` es único.
- `rating` debe estar entre 1 y 5.
- `stock` no puede ser negativo.
- `price` no puede ser negativo.
- `visible` permite ocultar libros que no deberían aparecer en el catálogo público.

---

## Scripts SQL

La actividad requiere entregar scripts DDL y DML para reconstruir la base de datos usada durante el desarrollo.

Los archivos se encuentran en:

```txt
database/catalogue/
```

### `01_catalogue_ddl.sql`

Contiene la definición de la base de datos y de la tabla `books`.

Incluye:

- Creación de la base de datos `catalogue_db`.
- Creación de la tabla `books`.
- Restricciones `UNIQUE`.
- Restricciones `CHECK`.
- Índices para mejorar búsquedas por título, autor, categoría, fecha, valoración y visibilidad.

Ejemplo de ejecución en PostgreSQL:

```bash
psql -U postgres -f database/catalogue/01_catalogue_ddl.sql
```

Si el script contiene `CREATE DATABASE catalogue_db`, primero se crea la base y luego se debe ejecutar el resto del script conectado a `catalogue_db`.

En `psql`:

```bash
psql -U postgres
```

Luego:

```sql
CREATE DATABASE catalogue_db;
\c catalogue_db
```

Después ejecutar las sentencias de creación de tabla.

### `02_catalogue_dml.sql`

Contiene datos iniciales para poblar el catálogo. Incluye más de 100 libros de prueba con `external_id`, título, autor, fecha de publicación, categoría, ISBN, valoración, visibilidad, stock, precio y fecha de creación.

Ejemplo de ejecución:

```bash
psql -U postgres -d catalogue_db -f database/catalogue/02_catalogue_dml.sql
```

Este archivo permite disponer de un catálogo amplio para probar filtros, paginación y ordenamiento.

---

## Configuración del proyecto

Archivo:

```txt
src/main/resources/application.yml
```

Configuración base:

```yaml
server:
  port: 8081

spring:
  application:
    name: catalogue-service

  datasource:
    url: jdbc:postgresql://localhost:5432/catalogue_db
    username: postgres
    password: TU_PASSWORD

  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        format_sql: true

eureka:
  client:
    enabled: false
```

Durante el desarrollo individual del microservicio, Eureka puede estar desactivado. Cuando el proyecto `back-end-eureka` esté levantado, se puede activar el registro en Eureka:

```yaml
eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
```

---

## Cómo iniciar el proyecto

### 1. Crear la base de datos

En PostgreSQL:

```sql
CREATE DATABASE catalogue_db;
```

### 2. Ejecutar DDL

Ejecutar el archivo:

```txt
database/catalogue/01_catalogue_ddl.sql
```

### 3. Ejecutar DML

Ejecutar el archivo:

```txt
database/catalogue/02_catalogue_dml.sql
```

### 4. Configurar credenciales

Editar:

```txt
src/main/resources/application.yml
```

Actualizar usuario y contraseña:

```yaml
spring:
  datasource:
    username: postgres
    password: TU_PASSWORD
```

### 5. Compilar el proyecto

Desde la carpeta del microservicio:

```bash
cd back-end-books-catalogue
```

En Windows PowerShell:

```bash
.\mvnw.cmd clean compile
```

O sin ejecutar tests:

```bash
.\mvnw.cmd clean install -DskipTests
```

### 6. Iniciar el microservicio

```bash
.\mvnw.cmd spring-boot:run
```

El servicio quedará disponible en:

```txt
http://localhost:8081
```

---

## Endpoints disponibles

Base URL:

```txt
http://localhost:8081/api/books
```

| Método | Endpoint | Descripción |
|---|---|---|
| `POST` | `/api/books` | Crea un nuevo libro |
| `GET` | `/api/books` | Lista libros con filtros, paginación y ordenamiento |
| `GET` | `/api/books/{externalId}` | Obtiene un libro por su identificador público |
| `PUT` | `/api/books/{externalId}` | Actualiza completamente un libro |
| `PATCH` | `/api/books/{externalId}` | Actualiza parcialmente un libro |
| `DELETE` | `/api/books/{externalId}` | Elimina un libro |

---

## Ejemplos de uso

### Crear libro

```http
POST http://localhost:8081/api/books
Content-Type: application/json
```

Body:

```json
{
  "title": "Cien años de soledad",
  "author": "Gabriel García Márquez",
  "publicationDate": "1967-05-30",
  "category": "Novela",
  "isbn": "9780307474728",
  "rating": 5,
  "visible": true,
  "stock": 10,
  "price": 19.99
}
```

### Listar libros

```http
GET http://localhost:8081/api/books?page=1&limit=10
```

Respuesta esperada:

```json
{
  "data": [
    {
      "externalId": "00000000-0000-0000-0000-000000000001",
      "title": "Cien años de soledad",
      "author": "Gabriel García Márquez",
      "publicationDate": "1967-05-30",
      "category": "Novela",
      "isbn": "9780307474728",
      "rating": 5,
      "visible": true,
      "stock": 10,
      "price": 19.99,
      "createdAt": "2026-05-19T15:30:00"
    }
  ],
  "meta": {
    "count": 1,
    "page": 1,
    "limit": 10,
    "totalPages": 1,
    "hasNext": false,
    "hasPrevious": false
  }
}
```

### Filtros disponibles

```http
GET http://localhost:8081/api/books?title=cien&page=1&limit=10
GET http://localhost:8081/api/books?author=garcia&page=1&limit=10
GET http://localhost:8081/api/books?category=novela&rating=5&page=1&limit=10
GET http://localhost:8081/api/books?visible=true&page=1&limit=10
GET http://localhost:8081/api/books?publicationDate=1967-05-30&page=1&limit=10
```

### Ordenar resultados

```http
GET http://localhost:8081/api/books?page=1&limit=10&sortBy=createdAt&sortDirection=DESC
```

Campos permitidos para ordenamiento:

```txt
title
author
publicationDate
category
isbn
rating
visible
stock
price
createdAt
```

### Obtener libro por externalId

```http
GET http://localhost:8081/api/books/00000000-0000-0000-0000-000000000001
```

### Actualizar libro completo

```http
PUT http://localhost:8081/api/books/00000000-0000-0000-0000-000000000001
Content-Type: application/json
```

```json
{
  "title": "Cien años de soledad",
  "author": "Gabriel García Márquez",
  "publicationDate": "1967-05-30",
  "category": "Novela latinoamericana",
  "isbn": "9780307474728",
  "rating": 5,
  "visible": true,
  "stock": 15,
  "price": 21.50
}
```

### Actualizar libro parcialmente

```http
PATCH http://localhost:8081/api/books/00000000-0000-0000-0000-000000000001
Content-Type: application/json
```

```json
{
  "stock": 25,
  "price": 22.99
}
```

### Eliminar libro

```http
DELETE http://localhost:8081/api/books/00000000-0000-0000-0000-000000000001
```

---

## Manejo de errores

El microservicio incluye un manejador global de excepciones mediante `@RestControllerAdvice`.

### Error por recurso no encontrado

```json
{
  "timestamp": "2026-05-19T15:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Libro no encontrado con externalId: 00000000-0000-0000-0000-000000000001",
  "path": "/api/books/00000000-0000-0000-0000-000000000001"
}
```

### Error por ISBN duplicado

```json
{
  "timestamp": "2026-05-19T15:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Ya existe un libro con el ISBN: 9780307474728",
  "path": "/api/books"
}
```

### Error de validación

```json
{
  "timestamp": "2026-05-19T15:30:00",
  "status": 400,
  "error": "Validation Error",
  "message": "rating: La valoracion maxima es 5",
  "path": "/api/books"
}
```

---

## Eureka Client

El microservicio está preparado para registrarse en Eureka Server.

Para ello, el `pom.xml` incluye:

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
</dependency>
```

Y el archivo `application.yml` debe contener:

```yaml
eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
```

Cuando `back-end-eureka` esté ejecutándose, `catalogue-service` se registrará con el nombre:

```txt
catalogue-service
```

Ese nombre corresponde a:

```yaml
spring:
  application:
    name: catalogue-service
```

Esto permitirá que otros microservicios, como `orders-service`, puedan comunicarse con el catálogo usando el nombre lógico del servicio y no una IP o puerto fijo.

---

## Comandos útiles

Compilar:

```bash
.\mvnw.cmd clean compile
```

Ejecutar:

```bash
.\mvnw.cmd spring-boot:run
```

Generar build:

```bash
.\mvnw.cmd clean install -DskipTests
```

Limpiar carpeta generada:

```bash
.\mvnw.cmd clean
```

---

## Notas de entrega

Para la entrega de la actividad, se debe incluir este proyecto dentro del ZIP final, sin la carpeta `target`.

Estructura recomendada:

```txt
relatos-papel-backend/
├── back-end-books-catalogue/
├── back-end-books-orders/
├── back-end-eureka/
├── back-end-gateway/
└── database/
    └── catalogue/
        ├── 01_catalogue_ddl.sql
        └── 02_catalogue_dml.sql
```

No incluir:

```txt
target/
.idea/
*.iml
out/
logs/
```
---

## Uso de inteligencia artificial

Durante el desarrollo del microservicio `catalogue-service` se utilizó IA generativa como apoyo para acelerar tareas de diseño, implementación y documentación técnica.

El uso de IA se aplicó principalmente en:

- Apoyo para estructurar la arquitectura por capas del microservicio.
- Generación inicial de clases DTO, entidad, repositorio, servicio y controlador.
- Apoyo en la construcción de filtros dinámicos con Spring Data JPA Specifications.
- Generación de ejemplos de datos para el archivo `02_catalogue_dml.sql`.
- Revisión de errores de Maven, configuración de JDK, dependencias y ejecución local.

### Estimación del uso de IA

| Métrica solicitada | Estimación aproximada |
|---|---:|
| Porcentaje de respuestas correctas o parcialmente correctas | 85 % |
| Porcentaje de respuestas incorrectas o que requirieron corrección | 15 % |
| Número aproximado de líneas de código generadas usando IA | 450 - 600 líneas |
| Estimación del tiempo ahorrado en codificación y documentación | 6 - 8 horas |

### Observaciones

Las respuestas generadas por IA fueron revisadas y adaptadas manualmente antes de integrarse al proyecto. Algunas recomendaciones iniciales tuvieron que ajustarse al paquete real del proyecto, a la estructura del repositorio, al uso de `externalId` como identificador público y a la configuración local de Maven, JDK y PostgreSQL.

La IA fue utilizada como herramienta de apoyo, pero la integración final, pruebas con Postman, configuración del entorno y validación del funcionamiento del microservicio fueron realizadas manualmente.

