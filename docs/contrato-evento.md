# Contrato del evento CATEGORIA_CREADA

Evento que publica ms-catalogo (EC2 A) cuando se crea una categoría válida y que consume ms-solicitudes (EC2 B) para guardar su copia local.

## Recursos de RabbitMQ

| Recurso | Valor |
|---|---|
| Virtual host | mesatech |
| Exchange | catalogo.exchange (direct, durable) |
| Routing key | categoria.creada |
| Cola | solicitudes.catalogo.queue (classic, durable, sin autoeliminación) |
| Binding | catalogo.exchange a solicitudes.catalogo.queue con categoria.creada |
| Usuario publicador | mesatech_publicador: configure `^$`, write `^catalogo\.exchange$`, read `^$` |
| Usuario consumidor | mesatech_consumidor: configure `^$`, write `^$`, read `^solicitudes\.catalogo\.queue$` |

Los usuarios de aplicación no tienen tags, así que no pueden entrar a la interfaz de administración. El publicador solo puede escribir en el exchange y el consumidor solo puede leer la cola. Ninguno puede crear ni borrar recursos. Los recursos se crean con `infra/rabbitmq/configurar-rabbitmq.sh`.

## Cuerpo del mensaje (JSON, UTF-8)

| Campo | Tipo | Descripción |
|---|---|---|
| eventId | String (UUID v4) | Identificador único del evento. Se repite como message_id de AMQP y como id de correlación de la confirmación. |
| tipoEvento | String | Valor fijo `CATEGORIA_CREADA`. |
| fechaEvento | String | Instante de publicación en ISO-8601 UTC, con fracción de segundo variable (formato de `java.time.Instant`), por ejemplo `2026-11-05T01:46:41.749551929Z`. |
| categoriaId | Long | Id original de la categoría en RDS Catálogo. En RDS Solicitudes es la clave primaria de la copia, así que conserva la referencia de origen. |
| nombre | String | Nombre de la categoría (máximo 100 caracteres). |

## Propiedades AMQP

| Propiedad | Valor |
|---|---|
| delivery_mode | 2 (persistente) |
| message_id | igual a eventId |
| type | CATEGORIA_CREADA |
| content_type | application/json |
| content_encoding | UTF-8 |

## Ejemplo real

Mensaje de la categoría Monitores, inspeccionado en la interfaz de RabbitMQ con "Nack message requeue true" (evidencia 115), es decir, sin retirarlo de la cola:

```json
{"eventId":"5efcf3f7-ce37-455a-b107-26940fc77f45","tipoEvento":"CATEGORIA_CREADA","fechaEvento":"2026-11-05T01:46:41.749551929Z","categoriaId":6,"nombre":"Monitores"}
```

Exchange catalogo.exchange, routing key categoria.creada, message_id 5efcf3f7-ce37-455a-b107-26940fc77f45, delivery_mode 2.

## Comportamiento del publicador (ms-catalogo)

1. `POST /api/catalogo/categorias` valida la solicitud y guarda la categoría en RDS Catálogo. La transacción se confirma antes de publicar.
2. Publica el evento con confirmaciones correlacionadas (`publisher-confirm-type=correlated`), `mandatory=true` y `publisher-returns=true`, y espera hasta 10 segundos la confirmación.
3. Si recibe ack y el mensaje no fue retornado, responde 201 con la categoría y su eventId.
4. Si recibe nack, el mensaje vuelve por no tener ruta a una cola o no llega confirmación, responde 503 con `categoriaId`, `eventId` y `registroGuardado: true`, y el texto aclara que el registro no se revirtió.

Logs: `Publicando evento ... eventId=... categoriaId=...`, luego `Evento confirmado por RabbitMQ (ack) eventId=...` o `Publicación NO confirmada eventId=... categoriaId=...`.

## Comportamiento del consumidor (ms-solicitudes)

1. Escucha solicitudes.catalogo.queue con prefetch 1 y ack AUTO de Spring AMQP: el ack se envía solo cuando el listener termina sin excepción, es decir, después de guardar en RDS Solicitudes.
2. Convierte el JSON y guarda en `categorias_recibidas` (categoria_id, nombre, event_id, fecha_evento, fecha_recepcion).
3. Si falla el guardado, la excepción se propaga y el mensaje vuelve a la cola (`default-requeue-rejected=true`). Si el mensaje no trae categoriaId o nombre, se rechaza sin reencolar para no crear un ciclo infinito.
4. Una entrega repetida del mismo evento sobrescribe la misma fila, porque categoria_id es la clave primaria.

Log: `Evento recibido messageId=... eventId=... categoriaId=... nombre='...'`.

## Límites

- La confirmación del publicador indica que RabbitMQ aceptó el mensaje, no que el consumidor ya lo guardó.
- Guardar en RDS y publicar son operaciones separadas: si la publicación falla, la categoría queda solo en Catálogo (prueba P7). No hay outbox ni reintento automático.
- No se garantiza procesamiento exactamente una vez.
