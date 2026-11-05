#!/usr/bin/env bash
# Genera el archivo .env (permisos 600) pidiendo las claves por teclado; no quedan en el historial.
# Usar claves alfanumericas (sin $ ni comillas) para que Compose no intente interpolarlas.
set -euo pipefail
cd "$(dirname "$0")"
umask 077
read -r -s -p "Clave para mesatech_admin: " A; echo
read -r -s -p "Clave para mesatech_publicador: " P; echo
read -r -s -p "Clave para mesatech_consumidor: " C; echo
cat > .env <<X
RABBITMQ_ADMIN_USER=mesatech_admin
RABBITMQ_ADMIN_PASSWORD=$A
RABBITMQ_PUBLICADOR_USER=mesatech_publicador
RABBITMQ_PUBLICADOR_PASSWORD=$P
RABBITMQ_CONSUMIDOR_USER=mesatech_consumidor
RABBITMQ_CONSUMIDOR_PASSWORD=$C
X
chmod 600 .env
ls -l .env
echo "Variables definidas (sin valores):"; cut -d= -f1 .env
