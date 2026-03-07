#!/usr/bin/env bash
set -e
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(dirname "$SCRIPT_DIR")"
APP_IMAGE="ems-barbearia/app:latest"

echo "Creating k3d cluster..."
k3d cluster create ems-barbearia \
  --port 8080:30880 \
  --agents 1 \
  --wait

echo "Building app image..."
docker build -t "$APP_IMAGE" "$ROOT_DIR/app"

echo "Importing app image into k3d..."
k3d image import "$APP_IMAGE" -c ems-barbearia

echo "Creating secrets from .env (run from repo root: cp .env.example .env then edit)..."
"$SCRIPT_DIR/create-secret.sh"

echo "Applying manifests..."
kubectl apply -f "$SCRIPT_DIR/namespace.yaml"
kubectl apply -f "$SCRIPT_DIR/mariadb-deployment.yaml"
kubectl apply -f "$SCRIPT_DIR/app-deployment.yaml"

echo "Waiting for deployments..."
kubectl rollout status deployment/mariadb -n ems-barbearia --timeout=120s
kubectl rollout status deployment/app -n ems-barbearia --timeout=120s

echo "Done. Get app URL: kubectl get svc app -n ems-barbearia"
