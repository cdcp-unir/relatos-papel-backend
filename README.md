# relatos-papel-backend

Back-end de la aplicación **Relatos de Papel**, desarrollado como parte de la Actividad 2 de la asignatura **Desarrollo Web: Full Stack**.

El proyecto implementa una arquitectura parcial de microservicios con **Java**, **Spring Boot**, **Spring Data JPA**, **Netflix Eureka** y **Spring Cloud Gateway**. La solución incluye dos microservicios principales, un servidor de descubrimiento, un API Gateway y scripts SQL para reconstruir las bases de datos.

---

## Tabla de contenidos

- [Descripción general](#descripción-general)
- [Arquitectura](#arquitectura)
- [Estructura del repositorio](#estructura-del-repositorio)
- [Tecnologías utilizadas](#tecnologías-utilizadas)
- [Microservicios](#microservicios)
- [Bases de datos](#bases-de-datos)
- [Scripts DDL y DML](#scripts-ddl-y-dml)
- [Configuración local](#configuración-local)
- [Orden de arranque](#orden-de-arranque)
- [Endpoints principales](#endpoints-principales)
- [Comunicación entre microservicios](#comunicación-entre-microservicios)
- [Eureka Server](#eureka-server)
- [Cloud Gateway](#cloud-gateway)
- [Pruebas recomendadas](#pruebas-recomendadas)
- [Uso de inteligencia artificial](#uso-de-inteligencia-artificial)
- [Notas de entrega](#notas-de-entrega)

---

## Descripción general

Este repositorio contiene el back-end parcial de **Relatos de Papel**, una aplicación orientada a la compra de libros físicos y digitales.

La solución está compuesta por:

- `back-end-books-catalogue`: microservicio de catálogo.
- `back-end-books-orders`: microservicio operador de órdenes de compra.
- `back-end-eureka`: servidor de descubrimiento Netflix Eureka.
- `back-end-gateway`: servidor perimetral Spring Cloud Gateway.
- `database`: scripts DDL y DML para reconstruir las bases de datos.

Cada microservicio posee su propia base de datos relacional. `catalogue-service` utiliza `catalogue_db` y `orders-service` utiliza `orders_db`.

---

## Arquitectura

```txt
Cliente / Postman / Front-end
            |
            v
      Cloud Gateway
            |
    ---------------------
    |                   |
    v                   v
catalogue-service   orders-service
    ^                   |
    |                   |
    ---------------------
      Comunicación HTTP
      usando Eureka
```

El flujo principal de compra es:

```txt
Cliente
  |
  v
orders-service
  |
  |-- consulta por HTTP a catalogue-service
  |-- valida existencia del libro
  |-- valida visibilidad
  |-- valida stock disponible
  |
  v
orders_db
```

---

## Estructura del repositorio

```txt
relatos-papel-backend/
│
├── back-end-books-catalogue/
│   ├── src/
│   ├── pom.xml
│   ├── mvnw
│   ├── mvnw.cmd
│   └── README.md
│
├── back-end-books-orders/
│   ├── src/
│   ├── pom.xml
│   ├── mvnw
│   ├── mvnw.cmd
│   └── README.md
│
├── back-end-eureka/
│   ├── src/
│   ├── pom.xml
│   ├── mvnw
│   ├── mvnw.cmd
│   └── README.md
│
├── back-end-gateway/
│   ├── src/
│   ├── pom.xml
│   ├── mvnw
│   ├── mvnw.cmd
│   └── README.md
│
├── database/
│   ├── catalogue/
│   │   ├── 01_catalogue_ddl.sql
│   │   └── 02_catalogue_dml.sql
│   │
│   └── orders/
│       ├── 01_orders_ddl.sql
│       └── 02_orders_dml.sql
│
├── docker-compose.yml
├── .gitignore
└── README.md
```

---

## Tecnologías utilizadas

- Java 17
- Spring Boot 4.0.6
- Spring Web
- Spring WebFlux / WebClient
- Spring Data JPA
- Hibernate
- Jakarta Validation
- PostgreSQL
- Maven
- Netflix Eureka
- Spring Cloud Gateway
- Spring Cloud LoadBalancer
- Docker / Docker Compose
- Postman

---

## Microservicios

### catalogue-service

Microservicio encargado de gestionar el catálogo de libros.

Responsabilidades:

- Crear libros.
- Listar libros con filtros, paginación y ordenamiento.
- Buscar libros por `externalId`.
- Actualizar libros total o parcialmente.
- Eliminar libros.
- Permitir búsquedas por título, autor, fecha de publicación, categoría, ISBN, valoración y visibilidad.

Puerto local sugerido:

```txt
8081
```

Nombre registrado en Eureka:

```txt
catalogue-service
```

Base de datos:

```txt
catalogue_db
```

Endpoint base:

```txt
/api/v1/books
```

---

### orders-service

Microservicio operador encargado de registrar compras de libros.

Responsabilidades:

- Registrar órdenes de compra.
- Consultar `catalogue-service` antes de registrar la compra.
- Validar que el libro exista.
- Validar que el libro esté visible.
- Validar que exista stock suficiente.
- Actualizar el stock del libro después de la compra.
- Persistir el acuse de compra en su base de datos.
- Recuperar órdenes recientes de un usuario.

Puerto local sugerido:

```txt
8082
```

Nombre registrado en Eureka:

```txt
orders-service
```

Base de datos:

```txt
orders_db
```

Endpoint base:

```txt
/api/v1/orders
```

---

### eureka-server

Servidor de descubrimiento de servicios basado en Netflix Eureka.

Responsabilidades:

- Registrar automáticamente los microservicios al arrancar.
- Permitir que los microservicios se comuniquen usando nombres lógicos.
- Evitar el uso de IP y puerto en la comunicación interna.

Puerto local:

```txt
8761
```

Dashboard:

```txt
http://localhost:8761
```

Servicios esperados en Eureka:

```txt
CATALOGUE-SERVICE
ORDERS-SERVICE
CLOUD-GATEWAY
```

---

### cloud-gateway

Servidor perimetral de la arquitectura.

Responsabilidades:

- Actuar como punto único de entrada al back-end.
- Redirigir peticiones hacia `catalogue-service` y `orders-service`.
- Usar Eureka para localizar dinámicamente los microservicios.
- Evitar que el cliente acceda directamente a los puertos internos.

Puerto local:

```txt
8080
```

Ejemplos de rutas:

```txt
/catalogue/** -> catalogue-service
/orders/**    -> orders-service
```

---

## Bases de datos

Cada microservicio utiliza una base de datos independiente.

| Microservicio | Base de datos | Motor |
|---|---|---|
| `catalogue-service` | `catalogue_db` | PostgreSQL |
| `orders-service` | `orders_db` | PostgreSQL |

Esta separación evita que un microservicio dependa directamente de las tablas del otro.

---

## Scripts DDL y DML

Los scripts SQL se encuentran en la carpeta:

```txt
database/
```

### Catálogo

```txt
database/catalogue/01_catalogue_ddl.sql
database/catalogue/02_catalogue_dml.sql
```

- `01_catalogue_ddl.sql`: crea la base de datos y la tabla `books`.
- `02_catalogue_dml.sql`: inserta más de 100 libros de prueba.

### Órdenes

```txt
database/orders/01_orders_ddl.sql
database/orders/02_orders_dml.sql
```

- `01_orders_ddl.sql`: crea la base de datos y las tablas de órdenes.
- `02_orders_dml.sql`: inserta datos iniciales o de prueba para órdenes.

---

## Configuración local

Cada microservicio tiene su propio archivo:

```txt
src/main/resources/application.yml
```

### catalogue-service

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

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
```

### orders-service

```yaml
server:
  port: 8082

spring:
  application:
    name: orders-service

  datasource:
    url: jdbc:postgresql://localhost:5432/orders_db
    username: postgres
    password: TU_PASSWORD

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/

booksCatalogue:
  url: http://catalogue-service/api/v1
```

La propiedad importante es:

```yaml
booksCatalogue:
  url: http://catalogue-service/api/v1
```

Esta URL usa el nombre lógico del microservicio registrado en Eureka, no `localhost:8081`.

---

## Orden de arranque

Para ejecutar correctamente la arquitectura en local, se recomienda iniciar los componentes en este orden:

```txt
1. PostgreSQL / Docker Compose
2. eureka-server
3. catalogue-service
4. orders-service
5. cloud-gateway
```

### Levantar bases de datos con Docker

```bash
docker compose up -d
```

### Levantar Eureka

```bash
cd back-end-eureka
.\mvnw.cmd spring-boot:run
```

Verificar:

```txt
http://localhost:8761
```

### Levantar catalogue-service

```bash
cd back-end-books-catalogue
.\mvnw.cmd spring-boot:run
```

### Levantar orders-service

```bash
cd back-end-books-orders
.\mvnw.cmd spring-boot:run
```

### Levantar cloud-gateway

```bash
cd back-end-gateway
.\mvnw.cmd spring-boot:run
```

---

## Endpoints principales

### catalogue-service directo

```http
GET    http://localhost:8081/api/v1/books
GET    http://localhost:8081/api/v1/books/{externalId}
POST   http://localhost:8081/api/v1/books
PUT    http://localhost:8081/api/v1/books/{externalId}
PATCH  http://localhost:8081/api/v1/books/{externalId}
DELETE http://localhost:8081/api/v1/books/{externalId}
```

### orders-service directo

```http
POST http://localhost:8082/api/v1/orders
GET  http://localhost:8082/api/v1/orders/users/{ownerId}/recent
```

### Mediante cloud-gateway

```http
GET  http://localhost:8080/catalogue/api/v1/books
POST http://localhost:8080/orders/api/v1/orders
GET  http://localhost:8080/orders/api/v1/orders/users/{ownerId}/recent
```

---

## Comunicación entre microservicios

`orders-service` se comunica con `catalogue-service` usando HTTP y el nombre lógico registrado en Eureka.

Ejemplo:

```txt
http://catalogue-service/api/v1/books/{externalId}
```

Esto evita usar:

```txt
http://localhost:8081/api/v1/books/{externalId}
```

Para que funcione, `orders-service` usa un `WebClient.Builder` con `@LoadBalanced`.

```java
@Bean
@LoadBalanced
public WebClient.Builder webClientBuilder() {
    return WebClient.builder();
}
```

---

## Eureka Server

Al iniciar los microservicios, el dashboard de Eureka debe mostrar:

```txt
CATALOGUE-SERVICE
ORDERS-SERVICE
CLOUD-GATEWAY
```

URL:

```txt
http://localhost:8761
```

En desarrollo local, si aparece el aviso de autopreservación, puede desactivarse con:

```yaml
eureka:
  server:
    enable-self-preservation: false
```

---

## Cloud Gateway

El gateway funciona como entrada única al back-end.

Ejemplo de configuración conceptual:

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: catalogue-service
          uri: lb://catalogue-service
          predicates:
            - Path=/catalogue/**
          filters:
            - StripPrefix=1

        - id: orders-service
          uri: lb://orders-service
          predicates:
            - Path=/orders/**
          filters:
            - StripPrefix=1
```

Con esta configuración, una petición a:

```txt
http://localhost:8080/catalogue/api/v1/books
```

se redirige a:

```txt
catalogue-service/api/v1/books
```

Y una petición a:

```txt
http://localhost:8080/orders/api/v1/orders
```

se redirige a:

```txt
orders-service/api/v1/orders
```

---

## Pruebas recomendadas

### Probar catálogo

```http
GET http://localhost:8081/api/v1/books
```

### Probar catálogo desde gateway

```http
GET http://localhost:8080/catalogue/api/v1/books
```

### Crear orden

```http
POST http://localhost:8082/api/v1/orders
Content-Type: application/json
```

Body de ejemplo:

```json
{
  "items": [
    {
      "externalId": "00000000-0000-0000-0000-000000000001",
      "quantity": 1
    }
  ]
}
```

### Crear orden desde gateway

```http
POST http://localhost:8080/orders/api/v1/orders
Content-Type: application/json
```

### Consultar órdenes recientes

```http
GET http://localhost:8082/api/v1/orders/users/1/recent
```

Mediante gateway:

```http
GET http://localhost:8080/orders/api/v1/orders/users/1/recent
```

### Casos negativos

Se recomienda probar:

- Compra de libro inexistente.
- Compra de libro oculto.
- Compra con stock insuficiente.
- Compra con cantidad menor o igual a cero.
- Consulta de órdenes recientes de un usuario sin compras.

---

## Uso de inteligencia artificial

Durante el desarrollo de esta actividad se utilizó IA generativa como apoyo para tareas de diseño, implementación y documentación.

El uso de IA se aplicó principalmente en:

- Construcción inicial de entidades, DTOs, servicios y controladores.
- Generación de scripts DDL y DML.
- Inserción de datos de prueba para el catálogo.
- Resolución de errores de Maven, JDK, rutas largas en Windows y configuración de IntelliJ.
- Configuración de Eureka Client y comunicación entre microservicios.
- Documentación técnica del proyecto.

### Estimación del uso de IA

| Métrica solicitada | Estimación aproximada |
|---|---:|
| Porcentaje de respuestas correctas o parcialmente correctas | 85 % |
| Porcentaje de respuestas incorrectas o que requirieron corrección | 15 % |
| Número aproximado de líneas de código generadas usando IA | 800 - 1200 líneas |
| Estimación del tiempo ahorrado en codificación y documentación | 10 - 15 horas |

Las respuestas generadas por IA fueron revisadas, adaptadas y probadas manualmente antes de integrarse al proyecto.

---

## Notas de entrega

La entrega debe incluir un único archivo ZIP con:

```txt
relatos-papel-backend/
├── back-end-books-catalogue/
├── back-end-books-orders/
├── back-end-eureka/
├── back-end-gateway/
├── database/
│   ├── catalogue/
│   │   ├── 01_catalogue_ddl.sql
│   │   └── 02_catalogue_dml.sql
│   └── orders/
│       ├── 01_orders_ddl.sql
│       └── 02_orders_dml.sql
└── README.md
```

No incluir:

```txt
target/
.idea/
*.iml
out/
logs/
```

La vídeo-memoria debe mostrar:

- Arranque de Eureka, Gateway y microservicios.
- Dashboard de Eureka con servicios registrados.
- Operaciones REST de `catalogue-service`.
- Operaciones REST de `orders-service`.
- Comunicación entre `orders-service` y `catalogue-service`.
- Pruebas desde Gateway.
- Explicación del uso de IA en el desarrollo.
