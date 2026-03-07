#!/usr/bin/env bash
set -e
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(dirname "$SCRIPT_DIR")"
ENV_FILE="${ROOT_DIR}/.env"
NAMESPACE="ems-barbearia"

if [[ ! -f "$ENV_FILE" ]]; then
  echo "Create $ENV_FILE from .env.example first."
  exit 1
fi

# shellcheck source=/dev/null
source "$ENV_FILE"

kubectl create namespace "$NAMESPACE" --dry-run=client -o yaml | kubectl apply -f -

kubectl create secret generic mariadb-secret -n "$NAMESPACE" \
  --from-literal=MARIADB_ROOT_PASSWORD="${MARIADB_ROOT_PASSWORD}" \
  --from-literal=MARIADB_DATABASE="${MARIADB_DATABASE}" \
  --from-literal=MARIADB_USER="${MARIADB_USER}" \
  --from-literal=MARIADB_PASSWORD="${MARIADB_PASSWORD}" \
  --dry-run=client -o yaml | kubectl apply -f -

kubectl create secret generic app-datasource -n "$NAMESPACE" \
  --from-literal=SPRING_DATASOURCE_USERNAME="${MARIADB_USER}" \
  --from-literal=SPRING_DATASOURCE_PASSWORD="${MARIADB_PASSWORD}" \
  --dry-run=client -o yaml | kubectl apply -f -

echo "Secrets created/updated in namespace $NAMESPACE."
