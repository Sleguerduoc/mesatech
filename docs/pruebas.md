# Pruebas P1 a P8 y evidencias

Hay dos series de pruebas. La serie A se ejecutó con comandos en las EC2 y deja las consultas SQL directas a cada RDS. La serie B repite P3 a P7 desde las consolas web (EC2, RabbitMQ Management y la aplicación React) y se respalda con consultas SQL de las mismas categorías (evidencias 151 y 152). Los números corresponden a los archivos de la carpeta de evidencias.

## P1 Separación real

- Preparación: tres EC2 en ejecución y dos instancias RDS disponibles.
- Resultado esperado: IDs de EC2 distintos, dos identificadores y endpoints RDS distintos, cada proceso conectado solo a su RDS.
- Resultado obtenido: EC2 A i-07de8b0bbf7651434 (ms-catalogo y BFF), EC2 B i-02e9b95772b730c63 (ms-solicitudes), EC2 C i-039277f13fa646dae (RabbitMQ). RDS mesatech-rds-catalogo y mesatech-rds-solicitudes, cada una con acceso solo desde el security group de su EC2.
- Evidencias: 104 a 107 (comandos), 124 a 127 (consola RDS).

## P2 Flujo completo

- Datos: categoría Redes (id 2), solicitud 2.
- Resultado esperado: fila en RDS Catálogo, evento publicado y confirmado, log del consumidor, fila en RDS Solicitudes y solicitud creada con la categoría recibida.
- Resultado obtenido: el esperado.
- Evidencias: 075 a 080.

## P3 Consumidor detenido

- Serie A: se detuvo el servicio mesatech-ms-solicitudes y se creó Software (id 3). Evidencias 081 a 084.
- Serie B: se detuvo la EC2 B completa desde la consola y se creó Teclados (id 7). Evidencias 128 a 131 y 151.
- Resultado esperado y obtenido: la categoría queda en RDS Catálogo, el mensaje queda Ready 1 en solicitudes.catalogo.queue sin consumidores y aún no existe en la copia local.

## P4 Consumo diferido

- Serie A: se inició el servicio y procesó Software. Evidencias 085 y 086.
- Serie B: se inició la EC2 B desde la consola y procesó Teclados. Evidencias 132 a 134 y 152.
- Resultado esperado y obtenido: la cola termina en Ready 0 y Unacked 0, y la categoría aparece en categorias_recibidas de RDS Solicitudes con el mismo eventId.

## P5 Reinicio del broker

- Serie A: consumidor detenido, categoría Impresoras (id 4). Evidencias 087 a 092.
- Serie B: EC2 B detenida desde la consola, categoría Audio (id 8). Evidencias 135 a 143 y 152.
- Recreación del contenedor (ambas series): `docker compose down` y `docker compose up -d` en la EC2 C, sin `-v`, así que se conservan el volumen mesatech_rabbitmq_data y el hostname mesatech-rabbitmq. Es el único paso de la serie B hecho por comando, porque no existe en ninguna consola.
- Resultado esperado y obtenido: después de recrear siguen el vhost, el exchange, la cola, el binding y el mensaje persistente (Ready 1). Al iniciar el consumidor, la cola queda en 0 y la categoría se guarda en RDS Solicitudes.
- En la serie A se inspeccionó el mensaje con Get messages usando la opción que lo devuelve a la cola (evidencias 115 y 116).

## P6 Reinicio de aplicaciones

- Serie A: se reiniciaron los servicios systemd de ms-solicitudes y ms-catalogo. Evidencias 093 a 096.
- Serie B: se reiniciaron las instancias EC2 A y B desde la consola (Reboot). Evidencias 144 a 147.
- Resultado esperado y obtenido: los servicios arrancan solos y se consultan las solicitudes, el catálogo y la copia local guardados antes, también por v2.

## P7 Fallo de publicación

- Serie A: se detuvo RabbitMQ y se creó Accesos (id 5). Evidencias 097 a 100.
- Serie B: se detuvo la EC2 C desde la consola y se creó Cables (id 9). Evidencias 148 a 150, 151 y 152.
- Resultado esperado y obtenido: respuesta controlada 503 que indica que la categoría se guardó en el catálogo pero el evento no fue confirmado por RabbitMQ. La categoría existe en RDS Catálogo y no en la copia local (columna "En Solicitudes" queda Pendiente). Al restablecer el broker la cola está vacía, porque el mensaje nunca llegó a RabbitMQ.
- Inconsistencia: guardar en RDS y publicar son operaciones separadas, así que la categoría queda solo en Catálogo hasta que se vuelva a publicar. No se implementa recuperación automática (outbox o reintentos), porque el enunciado no la exige. Si se reenvía la solicitud HTTP, se crea otra categoría distinta; si RabbitMQ entregara dos veces un mensaje (por ejemplo, si el consumidor cae después de guardar y antes del ack), el consumidor guarda con categoria_id como clave primaria, así que la segunda entrega sobrescribe la misma fila en vez de duplicarla. Esto no equivale a una garantía de procesamiento exactamente una vez.
- Al detener la EC2 C cambia su IP pública (no tiene Elastic IP). La interfaz de administración pasa a la nueva IP; A y B siguen conectándose por la IP privada 172.31.26.114, que no cambia.

## P8 Continuidad de la EP1

- Login con Entra ID, consumo autorizado, 401 sin token o con token alterado, 403 con el rol cliente en una operación de administrador, 409 al resolver sin pasar por EN_PROCESO, y v1 y v2 disponibles.
- Evidencias: 101 a 103, 122 y 123.

## Red y accesos

Evidencias 153 a 159. Detalle en docs/red-y-accesos.md.
