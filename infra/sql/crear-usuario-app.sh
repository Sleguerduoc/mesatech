#!/usr/bin/env bash
# Crea el usuario de aplicación con permisos solo de datos (sin DDL) sobre una base.
# Uso: ./crear-usuario-app.sh <endpoint-rds> <usuario-maestro> <base> <usuario-app>
# Pide las contraseñas por teclado; no quedan en el historial ni en archivos.
set -euo pipefail
HOST=$1; MAESTRO=$2; BASE=$3; APP=$4
read -r -s -p "Contraseña del usuario maestro ${MAESTRO}: " PASS_MAESTRO; echo
read -r -s -p "Contraseña nueva para ${APP}: " PASS_APP; echo
MYSQL_PWD="${PASS_MAESTRO}" mysql -h "${HOST}" -u "${MAESTRO}" <<SQL
CREATE USER IF NOT EXISTS '${APP}'@'%' IDENTIFIED BY '${PASS_APP}';
ALTER USER '${APP}'@'%' IDENTIFIED BY '${PASS_APP}';
GRANT SELECT, INSERT, UPDATE, DELETE ON ${BASE}.* TO '${APP}'@'%';
FLUSH PRIVILEGES;
SHOW GRANTS FOR '${APP}'@'%';
SQL
