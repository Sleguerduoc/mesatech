#!/usr/bin/env bash
# Crea los recursos de mensajeria de MesaTech en el broker levantado con compose.yaml.
# Se puede ejecutar mas de una vez (es idempotente). Ejecutar en la EC2 C, en esta misma carpeta.
set -euo pipefail
cd "$(dirname "$0")"

set -a; source .env; set +a

VHOST=mesatech
EXCHANGE=catalogo.exchange
QUEUE=solicitudes.catalogo.queue
ROUTING_KEY=categoria.creada
API=http://localhost:15672/api

ctl() { docker compose exec -T rabbitmq rabbitmqctl "$@"; }
api() { curl -sS -f -u "${RABBITMQ_ADMIN_USER}:${RABBITMQ_ADMIN_PASSWORD}" -H "content-type: application/json" "$@"; }

echo "== Esperando al broker"
until ctl await_startup >/dev/null 2>&1; do sleep 3; done

echo "== Virtual host ${VHOST}"
ctl add_vhost "${VHOST}" 2>/dev/null || echo "   (ya existia)"

crear_usuario() {
  local usuario=$1 clave=$2
  # La contrasena se entrega por stdin para que no quede en el historial ni en la lista de procesos
  if ctl list_users --formatter json | grep -q "\"${usuario}\""; then
    printf '%s' "${clave}" | ctl change_password "${usuario}" >/dev/null
    echo "   ${usuario}: contrasena actualizada"
  else
    printf '%s' "${clave}" | ctl add_user "${usuario}" >/dev/null
    echo "   ${usuario}: creado"
  fi
  # Sin tags: no puede entrar a la interfaz de administracion
  ctl set_user_tags "${usuario}" >/dev/null
}

echo "== Usuarios de aplicacion (sin privilegios administrativos)"
crear_usuario "${RABBITMQ_PUBLICADOR_USER}" "${RABBITMQ_PUBLICADOR_PASSWORD}"
crear_usuario "${RABBITMQ_CONSUMIDOR_USER}" "${RABBITMQ_CONSUMIDOR_PASSWORD}"

echo "== Permisos sobre ${VHOST} (configure / write / read)"
# Publicador: solo escribir en el exchange. No puede crear ni borrar recursos ni leer colas.
ctl set_permissions -p "${VHOST}" "${RABBITMQ_PUBLICADOR_USER}" '^$' '^catalogo\.exchange$' '^$'
# Consumidor: solo leer (consumir) la cola. No puede publicar ni configurar.
ctl set_permissions -p "${VHOST}" "${RABBITMQ_CONSUMIDOR_USER}" '^$' '^$' '^solicitudes\.catalogo\.queue$'
# El administrador tambien necesita permisos en el vhost para declarar los recursos
ctl set_permissions -p "${VHOST}" "${RABBITMQ_ADMIN_USER}" '.*' '.*' '.*'

echo "== Exchange ${EXCHANGE} (direct, durable)"
api -X PUT "${API}/exchanges/${VHOST}/${EXCHANGE}" \
  -d '{"type":"direct","durable":true,"auto_delete":false,"internal":false,"arguments":{}}'

echo "== Cola ${QUEUE} (durable, sin autoeliminacion)"
api -X PUT "${API}/queues/${VHOST}/${QUEUE}" \
  -d '{"durable":true,"auto_delete":false,"arguments":{"x-queue-type":"classic"}}'

echo "== Binding ${EXCHANGE} -> ${QUEUE} con routing key ${ROUTING_KEY}"
api -X POST "${API}/bindings/${VHOST}/e/${EXCHANGE}/q/${QUEUE}" \
  -d "{\"routing_key\":\"${ROUTING_KEY}\",\"arguments\":{}}"

echo "== Resultado"
ctl list_permissions -p "${VHOST}"
ctl list_exchanges -p "${VHOST}" name type durable auto_delete | grep -E "name|catalogo" || true
ctl list_queues -p "${VHOST}" name durable auto_delete messages_ready messages_unacknowledged
ctl list_bindings -p "${VHOST}" source_name destination_name routing_key
