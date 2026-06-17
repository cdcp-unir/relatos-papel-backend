# relatos-papel-backend

Back-end de la aplicación **Relatos de Papel**, desarrollado como parte de la Actividad 2 de la asignatura **Desarrollo
Web: Full Stack**.

El proyecto implementa una arquitectura parcial de microservicios con **Java**, **Spring Boot**, **Spring Data JPA**, *
*Netflix Eureka** y **Spring Cloud Gateway**. La solución incluye dos microservicios principales, un servidor de
descubrimiento, un API Gateway y scripts SQL para reconstruir las bases de datos.

---

## Bases de datos

Cada microservicio utiliza una base de datos independiente.

| Microservicio       | Base de datos  | Motor      |
|---------------------|----------------|------------|
| `catalogue-service` | `catalogue_db` | PostgreSQL |
| `orders-service`    | `books_orders` | PostgreSQL |

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

* `01_catalogue_ddl.sql`: crea la base de datos y la tabla `books`.
* `02_catalogue_dml.sql`: inserta más de 100 libros de prueba.

### Órdenes

```txt
database/orders/01_orders_ddl.sql
```

* `01_orders_ddl.sql`: crea la base de datos y las tablas de órdenes.

---

## Configuración local

Cada microservicio tiene su propio archivo:

```txt
src/main/resources/application.yml
```

### catalogue-service

```yaml
server:
  port: 0

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
  port: 0

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
```

La propiedad importante es:

```yaml
booksCatalogue:
  url: http://catalogue-service/api/v1
```

Esta URL usa el nombre lógico del microservicio registrado en Eureka, no `localhost:8081`.

## Consideraciones importantes de configuración

Antes de ejecutar el proyecto, se deben revisar algunos parámetros importantes para evitar errores de conexión entre los
microservicios, PostgreSQL, Eureka y el Gateway.

### 1. Cambiar credenciales de PostgreSQL

Las credenciales de base de datos definidas en los archivos `application.yml` de cada microservicio deben coincidir con
las credenciales configuradas en PostgreSQL o en `docker-compose.yml`.

Ejemplo de configuración en un microservicio:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/catalogue_db
    username: postgres
    password: TU_PASSWORD
```

En caso de usar Docker Compose, verificar también las variables del servicio PostgreSQL:

```yaml
environment:
  POSTGRES_USER: postgres
  POSTGRES_PASSWORD: TU_PASSWORD
  POSTGRES_DB: postgres
```

Es importante reemplazar `TU_PASSWORD` por la contraseña real utilizada en el entorno local o de pruebas.

Si la contraseña de PostgreSQL no coincide entre `docker-compose.yml` y los archivos `application.yml`, los
microservicios no podrán conectarse a sus respectivas bases de datos.

---

### 2. Verificar puertos cuando estén configurados en `0`

Algunos microservicios pueden estar configurados inicialmente con:

```yaml
server:
  port: 0
```

Cuando el puerto se establece en `0`, Spring Boot asigna automáticamente un puerto aleatorio disponible al iniciar el
servicio.

Esto es válido para servicios registrados en Eureka, porque Eureka permite descubrir dinámicamente en qué puerto se
encuentra cada microservicio. Sin embargo, para hacer pruebas directas desde navegador, Postman o curl, se debe
verificar el puerto real asignado.

El puerto asignado puede verse en la consola del microservicio al iniciar, por ejemplo:

```txt
Tomcat started on port 54321
```

También puede verificarse desde el dashboard de Eureka:

```txt
http://localhost:8761
```

En Eureka se debe revisar la instancia registrada de cada servicio para confirmar el host y puerto real.

---

### 3. Diferencia entre acceso directo y acceso por Gateway

Si un microservicio usa puerto aleatorio, no se recomienda depender de su puerto directo para las pruebas principales.

En ese caso, lo recomendable es probar mediante el Gateway, por ejemplo:

```http
GET http://localhost:8080/catalogue/api/v1/books
POST http://localhost:8080/orders/api/v1/orders
```

El Gateway se encarga de localizar los microservicios registrados en Eureka usando sus nombres lógicos:

```txt
catalogue-service
orders-service
```

Por esta razón, aunque `catalogue-service` y `orders-service` estén usando puertos aleatorios, el cliente puede seguir
consumiendo los servicios mediante el Gateway.

---

### 4. Verificar servicios registrados en Eureka

Antes de probar las rutas del Gateway, se debe confirmar que Eureka muestre los servicios esperados:

```txt
CATALOGUE-SERVICE
ORDERS-SERVICE
CLOUD-GATEWAY
```

Si alguno no aparece registrado, el Gateway no podrá redirigir correctamente las peticiones hacia ese microservicio.

---

### 5. Recomendación para pruebas locales

Para facilitar las pruebas manuales desde Postman, navegador o curl, se pueden usar puertos fijos durante el desarrollo:

```yaml
server:
  port: 8081
```

```yaml
server:
  port: 8082
```

Y dejar los puertos aleatorios únicamente cuando se quiera simular un entorno más dinámico con descubrimiento de
servicios mediante Eureka.

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

### Levantar bases de datos con Docker o directamente todo el backend

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

> Nota: si `catalogue-service` u `orders-service` están configurados con `server.port: 0`, los puertos directos `8081` y
`8082` pueden no aplicar. En ese caso, se debe verificar el puerto real en la consola del servicio o en el dashboard de
> Eureka. Para pruebas generales, se recomienda consumir los endpoints mediante el Cloud Gateway.

---


