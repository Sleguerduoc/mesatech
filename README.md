# MesaTech Cloud, EP2 (DSY1107 Desarrollo Cloud Native I)

Continuación de la EP1. Catálogo y Solicitudes quedan en EC2 distintas, cada uno con su propia instancia Amazon RDS, y se integran con RabbitMQ instalado desde cero con Docker Compose en una tercera EC2. Se mantienen React con MSAL, Microsoft Entra ID, AWS API Gateway, el BFF con validación JWT, las reglas de estados y las versiones v1 y v2.

## Arquitectura

```
React (S3, HTTPS) --login--> Microsoft Entra ID
      | Bearer JWT
      v
API Gateway HTTP API (autorizador JWT, CORS)
      |
      v
EC2 A: BFF :8080 ----> ms-catalogo :8082 ----> RDS Catálogo
      |                     |
      |                     | publica CATEGORIA_CREADA (catalogo.exchange, categoria.creada)
      |                     v
      |              EC2 C: RabbitMQ (Docker Compose) -> solicitudes.catalogo.queue
      |                                                         |
      |                                                         | consume
      v                                                         v
EC2 B: ms-solicitudes :8081 ------------------------------> RDS Solicitudes
```

| Componente | Ubicación | Responsabilidad |
|---|---|---|
| ms-catalogo (publicador) | EC2 A, i-07de8b0bbf7651434 | Categorías y prioridades; guarda en RDS Catálogo y publica el evento |
| BFF | EC2 A | Valida el JWT de Entra ID, autoriza por scope y rol y coordina los dos microservicios |
| ms-solicitudes (consumidor) | EC2 B, i-02e9b95772b730c63 | Solicitudes y estados; consume el evento y guarda la copia local de categorías |
| RabbitMQ | EC2 C, i-039277f13fa646dae | Broker, Docker Compose en Ubuntu 24.04 LTS |
| RDS Catálogo | mesatech-rds-catalogo | Base mesatech_catalogo, propiedad exclusiva de ms-catalogo |
| RDS Solicitudes | mesatech-rds-solicitudes | Base mesatech_solicitudes, propiedad exclusiva de ms-solicitudes |
| Frontend | Bucket S3 mesatech-web-845923697924 | React servido por el endpoint HTTPS de S3 |

El BFF no accede a ninguna RDS ni publica o consume mensajes. Ningún microservicio lee la base del otro.

## Versiones

| Elemento | Versión |
|---|---|
| Java | OpenJDK 17 (paquete openjdk-17-jdk-headless de Ubuntu 24.04) |
| Spring Boot | 4.1.1 (Spring AMQP, Spring Data JPA, Spring Security OAuth2 Resource Server) |
| MySQL | Amazon RDS for MySQL 8.4, db.t4g.micro; driver mysql-connector-j, puerto 3306 |
| RabbitMQ | imagen oficial rabbitmq:4.3.7-management (Erlang 27.3.4) |
| Docker Engine | 29.9.3 |
| Docker Compose | plugin v5.7.0 |
| Sistema operativo EC2 | Ubuntu 24.04 LTS (A, B y C) |
| React | 19.3, react-scripts 5.0.1, @azure/msal-browser 5.23, @azure/msal-react 5.7 |

## Estructura del repositorio

| Ruta | Contenido |
|---|---|
| `ms-catalogo/` | Microservicio de catálogo y publicador |
| `ms-solicitudes/` | Microservicio de solicitudes y consumidor |
| `bff/` | Backend for Frontend |
| `mesatech-web/` | React con MSAL |
| `infra/rabbitmq/` | compose.yaml, .env.example y scripts del broker |
| `infra/sql/` | Scripts de tablas y del usuario de aplicación |
| `infra/deploy/` | Preparación de EC2, archivos .env.example, unidades systemd y scripts de despliegue |
| `docs/` | Contrato del evento, red y accesos, pruebas P1 a P8 |

Ningún archivo con valores reales (.env, contraseñas, tokens o llaves) está en el repositorio.

## Requisitos

- Cuenta AWS (Learner Lab) en us-east-1, con la VPC por defecto.
- Tenant de Microsoft Entra ID con las aplicaciones de la EP1: la API mesatech-api, con los scopes Catalogo.Access y Solicitudes.Access y los roles ADMINISTRADOR, OPERADOR y CLIENTE, y la SPA mesatech-web.
- Security groups según `docs/red-y-accesos.md`.

## Despliegue

Los recursos de AWS se crean desde la consola. Los comandos siguientes se ejecutan dentro de cada EC2, desde la raíz del repositorio.

### 1. RDS

Crear dos instancias RDS MySQL independientes, sin acceso público, cada una con un security group que acepte 3306 solo desde el security group de su EC2. Luego, desde la EC2 correspondiente:

```bash
# EC2 A: tablas de Catálogo y usuario de aplicación (pide las contraseñas por teclado)
mysql -h <endpoint-rds-catalogo> -u admin -p < infra/sql/01-catalogo.sql
./infra/sql/crear-usuario-app.sh <endpoint-rds-catalogo> admin mesatech_catalogo catalogo_app

# EC2 B
mysql -h <endpoint-rds-solicitudes> -u admin -p < infra/sql/02-solicitudes.sql
./infra/sql/crear-usuario-app.sh <endpoint-rds-solicitudes> admin mesatech_solicitudes solicitudes_app
```

El usuario de aplicación solo tiene SELECT, INSERT, UPDATE y DELETE sobre su base.

### 2. RabbitMQ en la EC2 C

```bash
./infra/deploy/instalar-docker.sh        # Docker Engine y plugin Compose desde el repositorio oficial
cd infra/rabbitmq
./crear-env.sh                           # crea .env (chmod 600) pidiendo las claves por teclado
sudo docker compose config --quiet       # valida compose.yaml
sudo docker compose pull
sudo docker compose up -d
sudo docker compose ps
sudo docker logs mesatech-rabbitmq
sudo docker exec mesatech-rabbitmq rabbitmq-diagnostics ping
./configurar-rabbitmq.sh                 # vhost, usuarios, permisos, exchange, cola y binding (idempotente)
```

`configurar-rabbitmq.sh` usa `docker compose` sin sudo: `instalar-docker.sh` agrega al usuario ubuntu al grupo docker, y el cambio se aplica al abrir una sesión nueva.

compose.yaml declara la imagen con versión explícita, el hostname fijo mesatech-rabbitmq, las credenciales leídas de .env, los puertos 5672 y 15672, el volumen mesatech_rabbitmq_data en /var/lib/rabbitmq, la política `restart: unless-stopped` y un healthcheck. La interfaz de administración queda en `http://<ip-publica-ec2-c>:15672`, accesible solo desde la IP administrativa.

### 3. Microservicios en las EC2 A y B

```bash
./infra/deploy/preparar-ec2-app.sh       # Java 17, cliente MySQL, /opt/mesatech y /etc/mesatech

# EC2 A
./infra/deploy/configurar-env.sh ms-catalogo DB_HOST=<endpoint-rds-catalogo> RABBITMQ_HOST=<ip-privada-ec2-c>
./infra/deploy/configurar-env.sh bff MS_SOLICITUDES_URL=http://<ip-privada-ec2-b>:8081 \
  JWT_ISSUER_URI=https://login.microsoftonline.com/<tenant-id>/v2.0 JWT_AUDIENCE=<client-id-api>
./infra/deploy/instalar-servicio.sh ms-catalogo
./infra/deploy/instalar-servicio.sh bff

# EC2 B
./infra/deploy/configurar-env.sh ms-solicitudes DB_HOST=<endpoint-rds-solicitudes> RABBITMQ_HOST=<ip-privada-ec2-c>
./infra/deploy/instalar-servicio.sh ms-solicitudes
```

`configurar-env.sh` crea `/etc/mesatech/<servicio>.env` (chmod 600) a partir del `.env.example` y pide cada contraseña por teclado. `instalar-servicio.sh` compila con `./mvnw clean package`, copia el jar a `/opt/mesatech` e instala y arranca la unidad systemd.

Variables por servicio (ver los `.env.example` en `infra/deploy/`):

| Servicio | Variables |
|---|---|
| ms-catalogo | SERVER_ADDRESS, SERVER_PORT, DB_HOST, DB_PORT, DB_NAME, DB_USER, DB_PASSWORD, RABBITMQ_HOST, RABBITMQ_PORT, RABBITMQ_VHOST, RABBITMQ_USER, RABBITMQ_PASSWORD, RABBITMQ_EXCHANGE, RABBITMQ_ROUTING_KEY |
| ms-solicitudes | SERVER_ADDRESS, SERVER_PORT, DB_*, RABBITMQ_HOST, RABBITMQ_PORT, RABBITMQ_VHOST, RABBITMQ_USER, RABBITMQ_PASSWORD, RABBITMQ_QUEUE |
| bff | SERVER_PORT, MS_CATALOGO_URL, MS_SOLICITUDES_URL, JWT_ISSUER_URI, JWT_AUDIENCE |

### 4. API Gateway

HTTP API con rutas GET, POST, PUT, PATCH y DELETE en `/v1/{proxy+}` y `/v2/{proxy+}`, integradas por HTTP al BFF (`http://<elastic-ip-ec2-a>:8080/v1/{proxy}` y `/v2/{proxy}`) y protegidas por el autorizador JWT de Entra ID. `GET /health` queda sin autorizador. CORS permite el origen del bucket S3 y `http://localhost:3000`, con los encabezados authorization y content-type. No se usa una ruta ANY, para que API Gateway responda solo el preflight OPTIONS.

### 5. Frontend

```bash
cd mesatech-web
cp .env.example .env.production   # REACT_APP_CLIENT_ID, REACT_APP_TENANT_ID, REACT_APP_API_CLIENT_ID, REACT_APP_API_URL
npm ci && npm run build
aws s3 sync build/ s3://<bucket-web>/ --delete   # requiere AWS CLI con credenciales del laboratorio
```

También se puede usar `./infra/deploy/publicar-frontend.sh <bucket-web>`, que hace los tres pasos.

La aplicación se abre en `https://<bucket-web>.s3.us-east-1.amazonaws.com/index.html`, y esa URL debe estar registrada como redirect URI de la SPA en Entra ID. Se usa el endpoint HTTPS de S3 porque MSAL exige HTTPS; CloudFront no está disponible en el Learner Lab.

## Endpoints

Expuestos por API Gateway (`https://g11339x5ve.execute-api.us-east-1.amazonaws.com`). Todos exigen Bearer JWT, salvo /health.

| Método y ruta | Autorización | Uso |
|---|---|---|
| GET /health | pública | Estado del BFF |
| GET /v1/perfil | token válido | Datos del usuario autenticado |
| GET /v1/catalogo | Catalogo.Access | Categorías y prioridades |
| POST /v1/catalogo/categorias | Catalogo.Access y rol ADMINISTRADOR | Crear categoría y publicar el evento (201 o 503) |
| PUT, DELETE /v1/catalogo/categorias/{id} | Catalogo.Access y ADMINISTRADOR | Mantener categorías (comportamiento EP1) |
| POST, PUT, DELETE /v1/catalogo/prioridades[/{id}] | Catalogo.Access y ADMINISTRADOR | Mantener prioridades |
| GET /v1/solicitudes/categorias | Solicitudes.Access | Copia local de categorías en ms-solicitudes |
| POST /v1/solicitudes | Solicitudes.Access y rol CLIENTE, OPERADOR o ADMINISTRADOR | Crear solicitud con una categoría de la copia local |
| GET /v1/solicitudes/mias | Solicitudes.Access | Solicitudes propias |
| GET /v1/solicitudes | Solicitudes.Access y OPERADOR o ADMINISTRADOR | Todas las solicitudes |
| GET /v1/solicitudes/{id} | Solicitudes.Access | Detalle |
| PATCH /v1/solicitudes/{id}/estado | Solicitudes.Access y OPERADOR o ADMINISTRADOR | Cambio de estado; 409 si la transición no es válida |
| GET /v2/solicitudes/mias | Solicitudes.Access | Versión 2 del listado propio |

Contrato del evento entre Catálogo y Solicitudes: `docs/contrato-evento.md`.

## Arranque, detención y recuperación

Servicios: `mesatech-ms-catalogo` y `mesatech-bff` en la EC2 A, `mesatech-ms-solicitudes` en la EC2 B. Los tres están habilitados en systemd y arrancan solos al iniciar la instancia. El contenedor de RabbitMQ arranca solo porque Docker está habilitado y el servicio tiene `restart: unless-stopped`.

```bash
sudo systemctl status|start|stop|restart mesatech-ms-catalogo
sudo journalctl -u mesatech-ms-catalogo -f
cd infra/rabbitmq && sudo docker compose ps     # en la EC2 C
```

Orden de recuperación:

1. RDS Catálogo y RDS Solicitudes en estado Available.
2. EC2 C. Verificar `docker compose ps` (healthy) o la interfaz en el puerto 15672. Si se detuvo la instancia, su IP pública cambia (la privada no).
3. EC2 B. ms-solicitudes se conecta a su RDS y a la cola, y procesa los mensajes que hayan quedado Ready.
4. EC2 A. ms-catalogo y el BFF. El publicador abre la conexión con RabbitMQ en la primera publicación.
5. Comprobar `GET /health` y la aplicación.

Orden de detención: EC2 A primero, para no generar eventos nuevos; luego EC2 B, cuando la cola esté en 0; por último EC2 C. Al recrear el contenedor no usar `docker compose down -v`, porque `-v` borra el volumen con las definiciones y los mensajes.

Administración desde el navegador: AWS Systems Manager, Session Manager (sin puertos de entrada). SSH solo desde la IP administrativa /32.

## Documentación

- `docs/contrato-evento.md`: evento, recursos y permisos de RabbitMQ, confirmaciones.
- `docs/red-y-accesos.md`: matriz de security groups, puertos del contenedor y firewall.
- `docs/pruebas.md`: pruebas P1 a P8 con datos, resultados y evidencias.
