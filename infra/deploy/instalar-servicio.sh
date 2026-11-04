#!/usr/bin/env bash
# Compila un microservicio desde el código fuente y lo instala como servicio systemd.
# Uso (desde la raíz del repositorio): ./infra/deploy/instalar-servicio.sh ms-catalogo
set -euo pipefail
SVC=$1
RAIZ=$(cd "$(dirname "$0")/../.." && pwd)
if [ ! -f "/etc/mesatech/${SVC}.env" ]; then
  echo "Falta /etc/mesatech/${SVC}.env (copiar desde infra/deploy/${SVC}.env.example)"; exit 1
fi
cd "${RAIZ}/${SVC}"
chmod +x mvnw
./mvnw -q -B clean package -DskipTests
cp target/*-SNAPSHOT.jar "/opt/mesatech/${SVC}.jar"
sudo cp "${RAIZ}/infra/deploy/mesatech-${SVC}.service" /etc/systemd/system/
sudo systemctl daemon-reload
sudo systemctl enable "mesatech-${SVC}"
sudo systemctl restart "mesatech-${SVC}"
sleep 3
systemctl --no-pager status "mesatech-${SVC}" | head -5
