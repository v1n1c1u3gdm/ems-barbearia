# ADR 0007 – Containers: Docker Compose e k3d

## Status

Aceito.

## Contexto

É necessário ambiente de desenvolvimento local reproduzível e uma forma de simular deploy em Kubernetes sem depender de cluster externo.

## Decisão

- **Desenvolvimento local:** Docker Compose na raiz com quatro serviços:
  - (1) MariaDB 10.11 (healthcheck, volume persistente);
  - (2) app (build de `app/Dockerfile`, context-path `/api`, sem porta no host);
  - (3) ui (build de `ui/Dockerfile`, Nginx servindo o front);
  - (4) proxy (Nginx, porta 80 ou alternativa; encaminha `/` → ui, `/api/` → app).
  - Credenciais via variáveis de ambiente; app após MariaDB saudável; proxy depende de app e ui (ver ADR 0008).
- **Simulação de Kubernetes:** k3d com manifests em `k3d/` (namespace, MariaDB com Secret e PVC, app com Secret de datasource). O app usa context-path `/api`; health checks em `/api/actuator/health`. Script `k3d/cluster.sh` cria o cluster, importa a imagem do app, cria secrets a partir de `.env` e aplica os manifests. Documentação em `k3d/README.md`.

Nenhuma senha ou dado sensível é versionada; uso de `.env` (local) e Secrets (k8s).

## Consequências

- Um único endereço, `http://localhost` (porta 80), serve UI, API e documentação.
- Desenvolvimento com `docker compose up --build -d`; testes de deploy com k3d seguindo o README do k3d.
- Manifests reutilizáveis como base para outros ambientes Kubernetes.
