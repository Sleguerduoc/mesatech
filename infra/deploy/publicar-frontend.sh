#!/usr/bin/env bash
# Compila React y lo sube al bucket S3 que sirve CloudFront.
# Uso: ./infra/deploy/publicar-frontend.sh <bucket> [<id-distribucion-cloudfront>]
# Requiere mesatech-web/.env.production (copiar desde .env.example).
set -euo pipefail
BUCKET=$1; DIST=${2:-}
cd "$(dirname "$0")/../../mesatech-web"
npm ci --no-audit --no-fund
npm run build
aws s3 sync build/ "s3://${BUCKET}/" --delete
if [ -n "${DIST}" ]; then
  aws cloudfront create-invalidation --distribution-id "${DIST}" --paths "/*"
fi
