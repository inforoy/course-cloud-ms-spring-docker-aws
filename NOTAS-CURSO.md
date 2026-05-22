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
  --network springcloud \
  -d mysql:8

# Levantar con volumen persistente (recomendado con Docker Compose)
# El volumen mysql_data persiste los datos aunque se haga docker compose down
# Solo se pierde si se ejecuta: docker compose down -v

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

### Restaurar la base de datos

Los dumps están en `/Users/roycalle/dev/projects/courses/course-files/Dump20260519/`.

```bash
docker exec -i mysql-local mysql -uroot -padmin db_springboot_cloud < /Users/roycalle/dev/projects/courses/course-files/Dump20260519/db_springboot_cloud_products.sql
docker exec -i mysql-local mysql -uroot -padmin db_springboot_cloud < /Users/roycalle/dev/projects/courses/course-files/Dump20260519/db_springboot_cloud_roles.sql
docker exec -i mysql-local mysql -uroot -padmin db_springboot_cloud < /Users/roycalle/dev/projects/courses/course-files/Dump20260519/db_springboot_cloud_users.sql
docker exec -i mysql-local mysql -uroot -padmin db_springboot_cloud < /Users/roycalle/dev/projects/courses/course-files/Dump20260519/db_springboot_cloud_users_roles.sql
```

> Ejecutar en ese orden. Solo es necesario la primera vez o si se elimina el contenedor con `docker compose down -v`.

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

# Detener y eliminar TODOS los contenedores
docker stop $(docker ps -q)
docker rm $(docker ps -a -q)

# Eliminar TODAS las imágenes
docker rmi $(docker images -a -q)

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

### Problema: "bad credentials" después de restaurar la BD

Si el login falla con "bad credentials" pero el usuario sí se encuentra en los logs, el hash BCrypt del dump no coincide con el password actual. Regenerar y actualizar:

```bash
# 1. Generar nuevo hash para "12345"
CRYPTO=$(find ~/.m2/repository/org/springframework/security/spring-security-crypto -name "*.jar" | grep -v sources | head -1)
LOGGING=$(find ~/.m2/repository/commons-logging/commons-logging -name "*.jar" | grep -v sources | head -1)

cat > /tmp/BCryptGen.java << 'EOF'
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
public class BCryptGen {
    public static void main(String[] args) {
        System.out.println(new BCryptPasswordEncoder().encode("12345"));
    }
}
EOF

cd /tmp && javac -cp "$CRYPTO:$LOGGING" BCryptGen.java && java -cp "$CRYPTO:$LOGGING:." BCryptGen

# 2. Actualizar todos los usuarios con el nuevo hash (reemplazar el valor generado)
docker exec -i mysql-local mysql -uroot -padmin -e \
  "UPDATE db_springboot_cloud.users SET password='<hash-generado>';"
```

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

### Prueba de Load Balancing — múltiples instancias de ms-products

> Prueba obligatoria para verificar que el balanceo de carga funciona. Eureka registra todas las instancias y el gateway distribuye las requests entre ellas.

#### Con Docker Compose (recomendado)

Para usar `--scale`, el servicio **no debe tener `container_name` fijo** en el `docker-compose.yml` — comentarlo o eliminarlo:

```yaml
ms-products:
  # container_name: ms-products   # comentar para permitir múltiples instancias
  image: ms-products:latest
  ...
```

```bash
# Levantar 3 instancias de ms-products
docker compose up -d --scale ms-products=3

# Ver las instancias creadas
docker ps | grep ms-products
```

#### Con docker run (forma manual)

```bash
docker run -P --name ms-products2 --network springcloud -d ms-products
docker run -P --name ms-products3 --network springcloud -d ms-products
docker ps | grep ms-products
```

Verificar en Eureka que aparecen las 3 instancias: `http://localhost:8761`

Hacer varias requests al gateway y observar que el puerto varía entre respuestas:
```bash
curl http://localhost:8090/api/products
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

### Recompilar y redesplegar un microservicio

Pasos para cuando se hace un cambio en el código y hay que actualizar la imagen en Docker:

```bash
# 1. Detener y eliminar el contenedor
docker stop <nombre>
docker rm <nombre>

# 2. Eliminar la imagen
docker rmi <nombre>

# 3. Recompilar el JAR
cd <ruta-del-microservicio>
./mvnw package -DskipTests

# 4. Reconstruir la imagen
docker build -t <nombre> .

# 5. Levantar con compose
cd /Users/roycalle/dev/projects/courses/course-cloud-ms-spring-docker-aws/docker-compose
docker compose up -d <nombre>
```

---

## Docker Compose

```bash
# Levantar servicios por grupos (recomendado para respetar el orden de dependencias)

# Grupo 1 — infraestructura base
docker compose up -d config-server eureka-server mysql-local zipkin-server

# Grupo 2 — microservicios (después de que el grupo 1 esté healthy)
docker compose up -d ms-users ms-products ms-items ms-oauth

# Grupo 3 — gateway (último, depende de todos los anteriores)
docker compose up -d ms-gateway-server

# Levantar todos los servicios en foreground (ver logs en tiempo real)
docker compose up

# Levantar todos los servicios en background
docker compose up -d

# Ver logs de un servicio específico en tiempo real
docker compose logs -f eureka-server
docker compose logs -f config-server

# Detener todos los servicios
docker compose down

# Detener sin eliminar los contenedores
docker compose stop
```

| Parámetro | Significado |
|---|---|
| `up` | Crea e inicia los contenedores |
| `-d` | Detached — corre en background |
| `down` | Detiene y elimina contenedores y redes (el volumen `mysql_data` persiste) |
| `down -v` | Detiene, elimina contenedores, redes **y volúmenes** (se pierden los datos) |
| `stop` | Detiene los contenedores sin eliminarlos |
| `logs -f <servicio>` | Sigue los logs de un servicio específico |
| `up -d --force-recreate <servicio>` | Recrea un servicio específico con nuevas variables de entorno |

> **Primera vez que se levanta el compose:** el volumen `mysql_data` arranca vacío. Hay que restaurar las BDs manualmente una sola vez (ver sección de restauración de BD). A partir de entonces `docker compose down/up` no pierde los datos.

---

## Diferencias entre mi docker-compose y el de la clase

| Aspecto | Mi config | Clase |
|---|---|---|
| Nombre contenedor MySQL | `mysql-local` | `mysql8` |
| Imagen MySQL | `mysql:8` | `mysql:8.0.40` |
| Puerto MySQL | `3306:3306` | `3307:3306` |
| Password MySQL | `admin` | `sasa1234` |
| Volumen MySQL | `mysql_data:/var/lib/mysql` | No tiene |
| Usuario Zipkin | `root`/`admin` | `zipkin`/`zipkin` |
| MYSQL_JDBC_URL en Zipkin | Sí (fix RSA) | No |
| Prefijo microservicios | `ms-` | `msvc-` |
| Nombre gateway | `ms-gateway-server` | `gateway-server` |
| IP gateway | `192.168.1.44:9100` | `192.168.0.21:9100` |
| depends_on en gateway | Completo (todos los ms) | No tiene |

> **Por qué `MYSQL_JDBC_URL` en Zipkin:** la clase usa usuario `zipkin`/`zipkin` con MySQL 8.0.40, lo que evita el problema de autenticación RSA. Mi config usa `root`/`admin` con `caching_sha2_password`, que requiere `allowPublicKeyRetrieval=true` en la URL JDBC — sin ello Zipkin no puede conectarse.

> **Por qué el volumen en MySQL:** agregado para persistir los datos entre `docker compose down/up`. Sin él, hay que restaurar las BDs manualmente cada vez que se levanta el entorno desde cero.

---

## AWS — Amazon RDS

### Datos de la instancia

| Campo | Valor |
|---|---|
| Endpoint | `db-ms-products.crucyiuc89oi.us-east-2.rds.amazonaws.com` |
| Puerto | `3306` |
| Base de datos | `db_springboot_cloud` |
| Usuario | `root` |
| Password | `admin12345` |

> El Security Group `default` debe tener una regla de entrada: **MySQL/Aurora, puerto 3306, origen 0.0.0.0/0**.

### Restaurar la BD en AWS RDS

Requiere `mysql-client` instalado (`brew install mysql-client`). Agregar al PATH si no está:
```bash
export PATH="/opt/homebrew/opt/mysql-client/bin:$PATH"
```

```bash
mysql -h db-ms-products.crucyiuc89oi.us-east-2.rds.amazonaws.com -uroot -padmin12345 db_springboot_cloud < /Users/roycalle/dev/projects/courses/course-files/Dump20260519/db_springboot_cloud_products.sql
mysql -h db-ms-products.crucyiuc89oi.us-east-2.rds.amazonaws.com -uroot -padmin12345 db_springboot_cloud < /Users/roycalle/dev/projects/courses/course-files/Dump20260519/db_springboot_cloud_roles.sql
mysql -h db-ms-products.crucyiuc89oi.us-east-2.rds.amazonaws.com -uroot -padmin12345 db_springboot_cloud < /Users/roycalle/dev/projects/courses/course-files/Dump20260519/db_springboot_cloud_users.sql
mysql -h db-ms-products.crucyiuc89oi.us-east-2.rds.amazonaws.com -uroot -padmin12345 db_springboot_cloud < /Users/roycalle/dev/projects/courses/course-files/Dump20260519/db_springboot_cloud_users_roles.sql
```

> Ejecutar en ese orden. Solo es necesario la primera vez o si se elimina la instancia RDS.

---

## AWS — EC2

### Instancia: ec2-aws-springcloud

| Campo | Valor |
|---|---|
| Nombre | `ec2-aws-springcloud` |
| DNS público | `ec2-100-54-116-110.compute-1.amazonaws.com` |
| IP pública | `100.54.116.110` |
| Usuario SSH | `ec2-user` |
| Key pair | `ec2-aws.pem` |
| Ubicación del .pem | `docker-compose/ec2-aws.pem` |

### Instalar Docker en la instancia

```bash
# Instalar Docker
sudo yum install -y docker

# Arrancar el servicio Docker
sudo service docker start

# Verificar versión
sudo docker version
```

> Si tras instalar aparece `Cannot connect to the Docker daemon`, es que el servicio no está arrancado. Solución: `sudo service docker start`.

### Instalar Docker Compose en la instancia

```bash
# Descargar Docker Compose v5.1.2
sudo curl -SL https://github.com/docker/compose/releases/download/v5.1.2/docker-compose-linux-x86_64 \
  -o /usr/local/bin/docker-compose

# Dar permisos de ejecución
sudo chmod +x /usr/local/bin/docker-compose

# Crear symlink para poder usarlo sin ruta completa
sudo ln -s /usr/local/bin/docker-compose /usr/bin/docker-compose

# Verificar instalación
docker-compose version
```

### Conexión y transferencia de archivos

```bash
# Dar permisos al key pair (solo la primera vez)
chmod 400 docker-compose/ec2-aws.pem

# Conectar via SSH
ssh -i ec2-aws.pem ec2-user@ec2-100-54-116-110.compute-1.amazonaws.com

# Copiar docker-compose.yml a la instancia
scp -i ec2-aws.pem docker-compose.yml ec2-user@ec2-100-54-116-110.compute-1.amazonaws.com:~/.
```

> El archivo `ec2-aws.pem` está en `.gitignore` — nunca se sube al repositorio.

### Problema de arquitectura: arm64 (Mac) vs amd64 (EC2)

**Verificación:**

| Entorno | OS/Arch | Docker versión |
|---|---|---|
| Mac local (OrbStack) | `darwin/arm64` | 29.4.0 |
| EC2 AWS | `linux/amd64` | 25.0.14 |

Las imágenes publicadas en Docker Hub (`rwcalles/items`, `rwcalles/products`, `rwcalles/eureka`) fueron construidas en el Mac con arquitectura `arm64` — **no corren en EC2 que es `amd64`**.

**Error al hacer `sudo docker-compose up` en EC2:**
```
no matching manifest for linux/amd64 in the manifest list entries
```

**Solución:** reconstruir y subir las imágenes especificando plataforma `linux/amd64`:

```bash
# Desde el directorio de cada microservicio en el Mac
docker build --platform=linux/amd64 -t rwcalles/eureka .
docker build --platform=linux/amd64 -t rwcalles/products .
docker build --platform=linux/amd64 -t rwcalles/items .

# Verificar que la arquitectura sea amd64 antes de subir
docker image inspect rwcalles/eureka | grep Architecture

# Subir a Docker Hub (requiere docker login previo)
docker push rwcalles/eureka
docker push rwcalles/products
docker push rwcalles/items
```

> Verificar que el inspect muestre `"Architecture": "amd64"` antes de hacer push.

---

## AWS — Credenciales (solo demo)

| Campo | Valor |
|---|---|
| Email | `roycalle.dev@gmail.com` |
| Password | `boBwig-9tyscy-tajdiw` |
| Tipo | Usuario raíz |

> Cuenta creada exclusivamente para esta demo del curso. Eliminar recursos al finalizar para evitar costos.

---

## Docker Hub

Las imágenes publicadas en Docker Hub bajo el usuario `rwcalles`:

| Imagen | Repositorio |
|---|---|
| ms-items | `rwcalles/items` |
| ms-products | `rwcalles/products` |
| eureka-server | `rwcalles/eureka` |

```bash
# Buildear con tag para Docker Hub
docker build -t rwcalles/items .
docker build -t rwcalles/products .
docker tag eureka-server:latest rwcalles/eureka

# Subir a Docker Hub (requiere docker login)
docker login
docker push rwcalles/items
docker push rwcalles/products
docker push rwcalles/eureka
```

> Para el despliegue en AWS se usan solo: `eureka-server`, `ms-products`, `ms-items`. MySQL lo provee Amazon RDS. Los demás microservicios (`ms-users`, `ms-oauth`, `config-server`, `ms-gateway-server`, `zipkin`) no se despliegan en esta etapa.

---

## Prueba local conectando a AWS RDS

Verificación exitosa: levantar los 3 servicios localmente apuntando al RDS de AWS.

```bash
cd /Users/roycalle/dev/projects/courses/course-cloud-ms-spring-docker-aws/docker-compose
docker network create springcloud   # si no existe
docker compose up -d eureka-server
# esperar ~15 segundos
docker compose up -d ms-products ms-items
```

> **Timing de Eureka:** esperar al menos 30 segundos después de levantar `ms-products` antes de hacer requests a `ms-items`. Eureka tarda en propagar el registro — si se llama antes, `ms-items` responde 503 (`No servers available for service: ms-products`) aunque `ms-products` esté corriendo. No es un error de configuración, solo hay que esperar.

> `ms-products` se conecta al RDS de AWS via las variables de entorno del `docker-compose.yml` (`MYSQL_URL`, `MYSQL_USER`, `MYSQL_PASSWORD`). No se necesita MySQL local.

---

## Pendientes

- [x] Dockerfiles creados: `config-server`, `ms-products`, `ms-items`, `ms-users`, `ms-oauth`, `ms-gateway-server`
- [x] Crear Dockerfile para `eureka-server`
- [x] Imágenes publicadas en Docker Hub: `rwcalles/items`, `rwcalles/products`, `rwcalles/eureka`
- [x] Crear docker-compose.yml para levantar todos los servicios juntos
- [ ] Despliegue en AWS EC2
