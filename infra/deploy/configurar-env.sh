#!/usr/bin/env bash
# Crea /etc/mesatech/<servicio>.env a partir del .env.example, aplicando los valores no secretos que se
# pasan como argumentos y pidiendo por teclado cada *_PASSWORD (no quedan en el historial).
# Uso: ./configurar-env.sh ms-catalogo DB_HOST=... RABBITMQ_HOST=...
set -euo pipefail
SVC=$1; shift
DIR=$(cd "$(dirname "$0")" && pwd)
TMP=$(mktemp)
grep -v '^#' "${DIR}/${SVC}.env.example" | grep '=' > "${TMP}"
for par in "$@"; do
  clave=${par%%=*}; valor=${par#*=}
  sed -i "s|^${clave}=.*|${clave}=${valor}|" "${TMP}"
done
for clave in $(grep -o '^[A-Z_]*_PASSWORD' "${TMP}"); do
  read -r -s -p "Contrasena para ${clave}: " valor; echo
  sed -i "s|^${clave}=.*|${clave}=${valor}|" "${TMP}"
done
sudo install -m 600 -o ubuntu -g ubuntu "${TMP}" "/etc/mesatech/${SVC}.env"
rm -f "${TMP}"
echo "Creado /etc/mesatech/${SVC}.env (contrasenas ocultas):"
sudo sed -E 's/(PASSWORD=).*/\1********/' "/etc/mesatech/${SVC}.env"
