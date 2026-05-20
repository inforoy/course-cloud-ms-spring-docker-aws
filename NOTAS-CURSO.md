# Notas del Curso — Construye Microservicios Spring Boot, Eureka, Gateway, LoadBalancer, Resilience4J, Rest, OAuth 2.1, Docker, AWS EC2

## Arquitectura del Proyecto

```
API Gateway (8090)
    ├── ms-oauth        (9100)  — Autenticación OAuth2/JWT
    ├── ms-users        (8003)  — Gestión de usuarios (MySQL)
    ├── ms-products     (8001)  — Catálogo de productos (MySQL)
    └── ms-items        (8002)  — Agrega items desde ms-products (sin DB)

config-server   (8888)  — Configuración centralizada (Git)
eureka-server   (8761)  — Service Discovery (registro de servicios)
zipkin          (9411)  — Tracing distribuido
```

---

## Tecnologías

| Tecnología | Versión | Para qué sirve |
|---|---|---|
| Java | 21 | Runtime |
| Spring Boot | 3.4.4 | Framework principal |
| Spring Cloud | 2024.0.1 | Microservicios (Eureka, Gateway, Config) |
| MySQL | 8 | Base de datos (ms-products, ms-users) |
| Docker / OrbStack | — | Contenedores |
| SDKMAN | — | Gestor de versiones de Java |

---

## Entorno de Desarrollo

### Java con SDKMAN

```bash
# Ver versiones de Java instaladas
ls ~/.sdkman/candidates/java/

# Cambiar a Java 21 solo en la sesión actual de terminal
sdk use java 21.0.3-tem

# Cambiar Java 21 como versión default permanente del sistema
sdk default java 21.0.3-tem

# Ver versión activa
java -version
```

> El proyecto requiere Java 21 (`<java.version>21</java.version>` en pom.xml).
> Usar `sdk use` (no `sdk default`) para no afectar otras apps del sistema.

---

## Docker / OrbStack

> Se usa **OrbStack** en lugar de Docker Desktop por mejor compatibilidad con macOS reciente (Apple Silicon M4/M5) y arranque más rápido. Los comandos son 100% iguales a Docker Desktop.

> **Conflicto OrbStack vs Docker Desktop:** Si ambos están instalados, el contexto activo puede apuntar a Docker Desktop (que no está corriendo) y causar error de socket. Verificar y corregir:
> ```bash
> docker context list        # revisar cuál tiene el *
> docker context use orbstack
> ```

### MySQL

```bash
# Levantar contenedor MySQL con la configuración del proyecto
docker run --name mysql-local \
  -e MYSQL_ROOT_PASSWORD=admin \
  -e MYSQL_DATABASE=db_springboot_cloud \
  -p 3306:3306 \
  -d mysql:8

# Ver contenedores corriendo
docker ps

# Detener el contenedor
docker stop mysql-local

# Volver a arrancarlo (sin perder datos)
docker start mysql-local

# Eliminar el contenedor
docker rm mysql-local
```

> La configuración del contenedor coincide exactamente con `application.properties`:
> - Password root: `admin`
> - Base de datos: `db_springboot_cloud`
> - Puerto: `3306`

### Redes Docker

```bash
# Crear una red para que los contenedores se comuniquen entre sí por nombre
docker network create springcloud

# Listar redes
docker network ls
```

> Sin una red compartida los contenedores están aislados. Con la red `springcloud`,
> un contenedor puede encontrar a otro por su nombre (ej: `mysql:3306` en vez de una IP).

> **Todos los contenedores del proyecto deben estar en la red `springcloud`**: `mysql-local`, `zipkin-server`, `eureka-server`, `config-server` y todos los microservicios. Si un contenedor no está en la red, agrégalo con:
> ```bash
> docker network connect springcloud <nombre-contenedor>
> ```

### Imágenes Docker

> **Si el microservicio depende de `libs-ms-commons`** (o cualquier librería local del proyecto), primero instalarla en el repositorio Maven local. `mvn compile` o `mvn package` no son suficientes — el artefacto debe estar en `~/.m2`:
> ```bash
> cd libs-ms-commons && ./mvnw install
> ```
> Sin este paso, el build del microservicio falla con `Could not find artifact`.

```bash
# Construir imagen de un microservicio (primero generar el .jar con Maven)
./mvnw clean package -DskipTests
docker build -t nombre_imagen:v1 .

# Etiquetar / renombrar una imagen
docker tag nombre_imagen nombre_nuevo

# Listar imágenes locales
docker images

# Eliminar una imagen
docker rmi nombre_imagen

# Eliminar imágenes sin usar
docker image prune

# Analizar detalles de una imagen
docker image inspect nombre_imagen
```

### Contenedores

```bash
# Levantar contenedor en background con puerto y nombre
docker run -d -p 8001:8001 --name nombre_contenedor nombre_imagen

# Levantar con red personalizada
docker run -d -p 8888:8888 --name config-server --network springcloud config-server:v1

# Listar contenedores corriendo
docker ps

# Listar todos (incluye detenidos)
docker ps -a

# Detener un contenedor
docker stop nombre_contenedor

# Arrancar un contenedor detenido
docker start nombre_contenedor

# Eliminar un contenedor
docker rm nombre_contenedor

# Eliminar contenedores detenidos
docker container prune

# Analizar detalles de un contenedor
docker container inspect nombre_contenedor

# Ver logs en tiempo real
docker attach nombre_contenedor
# Salir sin detener: Ctrl + P, luego Ctrl + Q
# Salir y detener:   Ctrl + C

# Ver logs del contenedor (sin conectarse)
docker logs nombre_contenedor

# Ver logs en tiempo real (equivalente a tail -f)
docker logs -f nombre_contenedor

# Ver todos los comandos disponibles de Docker
docker --help

# Ver ayuda de subcomandos específicos
docker image --help
docker container --help
docker run --help
docker ps --help
docker images --help
docker stop --help
docker start --help
docker rm --help
docker rmi --help
```

| Parámetro | Significado |
|---|---|
| `-d` | Detached — corre en background (no bloquea la terminal) |
| `-p 8888:8888` | Expone el puerto del contenedor al puerto de tu Mac |
| `--name` | Nombre del contenedor en la red |
| `--network springcloud` | Lo conecta a la red compartida |

---

## Microservicios

### Orden de arranque recomendado

1. `mysql-local` — debe ser el primero (config-server, ms-products y ms-users dependen de él)
2. `config-server` — configuración centralizada (todos los microservicios lo necesitan)
3. `zipkin-server` — tracing distribuido
4. `eureka-server` — registro de servicios
5. `ms-users` — **debe estar antes que ms-oauth** (oauth lo llama para autenticar)
6. `ms-oauth` — autenticación
7. `ms-products` — catálogo de productos
8. `ms-items` — depende de ms-products
9. `ms-gateway-server` — punto de entrada

### Base de datos

Los únicos microservicios con base de datos son:
- `ms-products` → tabla `products`
- `ms-users` → tablas `users`, `roles`, `users_roles`

Ambos apuntan a MySQL en `localhost:3306/db_springboot_cloud`.

---

## Comandos Maven

```bash
# Compilar y empaquetar (genera el .jar en /target)
./mvnw clean package

# Compilar sin correr tests
./mvnw clean package -DskipTests

# Correr el microservicio
./mvnw spring-boot:run
```

---

## Zipkin — Tracing Distribuido

Zipkin se clona fuera del repo del curso:

```bash
cd ~/dev/projects
git clone https://github.com/openzipkin/zipkin.git
```

### Generar el JAR

Zipkin requiere Java 21. Cambiar solo para la sesión actual:

```bash
sdk use java 21.0.3-tem
```

Compilar:

```bash
./mvnw -T1C -q --batch-mode -DskipTests --also-make -pl zipkin-server clean package
```

Genera tres JARs en `zipkin-server/target/`:

| JAR | Tamaño | Uso |
|---|---|---|
| `zipkin-server-3.6.2-SNAPSHOT-exec.jar` | 129M | **Este es el ejecutable** |
| `zipkin-server-3.6.2-SNAPSHOT-slim.jar` | 82M | Sin dependencias embebidas |
| `zipkin-server-3.6.2-SNAPSHOT.jar` | 167K | Solo clases, no ejecutable |

### Levantar Zipkin

```bash
java -jar zipkin-server/target/zipkin-server-3.6.2-SNAPSHOT-exec.jar
```

Disponible en: `http://localhost:9411`

Volver a Java 17 para el proyecto del curso:

```bash
sdk use java 17.0.11-tem
```

> `sdk use` es temporal (solo dura la sesión de terminal). Al cerrar, vuelve automáticamente al Java del `current` (17).

### Levantar Zipkin como contenedor Docker

```bash
docker run -d -p 9411:9411 --name zipkin-server --network springcloud \
  -e STORAGE_TYPE=mysql \
  -e MYSQL_USER=root \
  -e MYSQL_PASS=admin \
  -e MYSQL_HOST=mysql-local \
  -e MYSQL_JDBC_URL="jdbc:mariadb://mysql-local:3306/zipkin?allowPublicKeyRetrieval=true&useSSL=false" \
  zipkin-server:latest
```

> El parámetro clave es `MYSQL_JDBC_URL`. Sin él, Zipkin construye la URL internamente y omite `allowPublicKeyRetrieval=true`, lo que causa error de autenticación RSA con MySQL 8.

> `allowPublicKeyRetrieval=true` es obligatorio — MySQL 8 usa `caching_sha2_password` que requiere la clave RSA pública. Sin este parámetro Zipkin no puede autenticarse aunque el usuario y password sean correctos.

> `mysql-local` debe estar en la red `springcloud`. Si no está, ejecutar: `docker network connect springcloud mysql-local`

> **Nombre del host Zipkin en los microservicios:** la propiedad de tracing debe apuntar a `zipkin-server` (el nombre del contenedor en la red). Si el microservicio lanza `Failed to resolve 'zipkin'`, revisar el `application.properties` o la config en config-server y corregir la URL a `http://zipkin-server:9411`.

### Base de datos

- Zipkin tiene su propia BD `zipkin` en MySQL con las tablas: `zipkin_annotations`, `zipkin_dependencies`, `zipkin_spans`
- Los microservicios **no** se conectan a esta BD — solo envían trazas por HTTP al puerto `9411`
- La BD la usa Zipkin internamente para persistir las trazas

---

## OAuth2 — Obtener Token (Insomnia)

El flujo es **Authorization Code**. `ms-oauth` llama internamente a `ms-users` para verificar credenciales — si `ms-users` no está corriendo, el login falla con "bad credentials" aunque el usuario exista.

### Configuración en Insomnia

| Campo | Valor |
|---|---|
| Grant Type | `Authorization Code` |
| Authorization URL | `http://192.168.1.44:9100/oauth2/authorize` |
| Access Token URL | `http://192.168.1.44:9100/oauth2/token` |
| Client ID | `gateway-app` |
| Client Secret | `12345` |
| Redirect URL | `http://127.0.0.1:8090/login/oauth2/code/client-app` |
| Use PKCE | desactivado |

> **Por qué esta combinación de IPs:** el gateway valida tokens con `issuer-uri: ${IP_ADDR}` (`192.168.1.44:9100`), por eso el token debe ser emitido accediendo a ms-oauth por esa IP. El redirect debe ser `127.0.0.1` porque así está registrado en `SecurityConfig.java` de ms-oauth — si no coincide exactamente, el servidor devuelve 400.

### Credenciales de usuarios (BD)

Todos los usuarios tienen password `12345`. Usuarios con rol `ROLE_ADMIN`:
- `lucas` / `12345`
- `Jhonny` / `12345`
- `mary` / `12345`

Usuarios con solo `ROLE_USER`:
- `sara` / `12345`
- `maria` / `12345`

### Permisos por rol (Gateway SecurityConfig)

| Endpoint | Acceso |
|---|---|
| `GET /api/items`, `/api/products`, `/api/users` | Público |
| `GET /api/*/\{id\}` | `ROLE_USER` o `ROLE_ADMIN` |
| `POST/PUT/DELETE /api/**` | Solo `ROLE_ADMIN` |

### Generar hash BCrypt para un password

```bash
CRYPTO=~/.m2/repository/org/springframework/security/spring-security-crypto/7.0.4/spring-security-crypto-7.0.4.jar
LOGGING=~/.m2/repository/commons-logging/commons-logging/1.3.6/commons-logging-1.3.6.jar

# Crear BCryptGen.java en /tmp con este contenido:
# import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
# public class BCryptGen {
#     public static void main(String[] args) {
#         System.out.println(new BCryptPasswordEncoder().encode("12345"));
#     }
# }

cd /tmp && javac -cp "$CRYPTO:$LOGGING" BCryptGen.java
java -cp "$CRYPTO:$LOGGING:." BCryptGen
```

---

## Notas y Pendientes

### Flujo completo para construir y correr ms-products

```bash
# 1. Instalar librería compartida en Maven local
cd libs-ms-commons && ./mvnw install

# 2. Empaquetar ms-products
cd ../ms-products && ./mvnw package -DskipTests

# 3. Construir imagen Docker
docker build -t ms-products .

# 4. Levantar dependencias y correr el contenedor
docker start zipkin-server
docker start eureka-server
docker run -P --name ms-products --network springcloud ms-products
```

### Flujo completo para construir y correr ms-users

```bash
# 1. Empaquetar ms-users
cd ms-users && ./mvnw package -DskipTests

# 2. Construir imagen Docker
docker build -t ms-users .

# 3. Correr el contenedor
docker run -P --name ms-users --network springcloud ms-users
```

### Flujo completo para construir y correr ms-oauth

> **Dependencia crítica:** ms-users debe estar corriendo antes — ms-oauth lo llama internamente para verificar credenciales.

```bash
# 1. Empaquetar ms-oauth
cd ms-oauth && ./mvnw package -DskipTests

# 2. Construir imagen Docker
docker build -t ms-oauth .

# 3. Correr el contenedor en background
docker run -p 9100:9100 --network springcloud --name ms-oauth -d ms-oauth
```

### Flujo completo para construir y correr ms-items

> **Dependencia crítica:** ms-items requiere `config-server` corriendo antes de arrancar — lo necesita para obtener su configuración.

```bash
# 1. Instalar librería compartida en Maven local
cd libs-ms-commons && ./mvnw install

# 2. Empaquetar ms-items
cd ../ms-items && ./mvnw package -DskipTests

# 3. Construir imagen Docker
docker build -t ms-items .

# 4. Levantar dependencias (en orden) y correr el contenedor
docker start config-server
docker start zipkin-server
docker start eureka-server
docker run -p 8005:8005 --name ms-items --network springcloud ms-items:latest
```

### Flujo completo para construir y correr ms-gateway-server

> **Nota:** si el proyecto no tiene Maven Wrapper, copiarlo desde otro microservicio:
> ```bash
> cp ../ms-items/mvnw . && cp ../ms-items/mvnw.cmd . && cp -r ../ms-items/.mvn . && chmod +x mvnw
> ```

```bash
# 1. Empaquetar ms-gateway-server
cd ms-gateway-server && ./mvnw package -DskipTests

# 2. Construir imagen Docker
docker build -t ms-gateway-server .

# 3. Correr el contenedor (IP_ADDR apunta a ms-oauth: IP de tu Mac + puerto 9100)
docker run -p 8090:8090 --network springcloud --name ms-gateway-server \
  -e IP_ADDR=192.168.1.44:9100 -d ms-gateway-server
```

---

## Pendientes

- [x] Dockerfiles creados: `config-server`, `ms-products`, `ms-items`, `ms-users`, `ms-oauth`, `ms-gateway-server`
- [ ] Crear Dockerfile para `eureka-server`
- [ ] Crear docker-compose.yml para levantar todos los servicios juntos
- [ ] Despliegue en AWS EC2
