# ADR 0006 – Estrutura do repositório: app/ e ui/

## Status

Aceito.

## Contexto

O projeto é full-stack (backend + frontend) em um único repositório. É necessário separar claramente as duas partes, simplificar builds e deploys e permitir que cada stack use suas próprias ferramentas.

## Decisão

- **Backend:** Todo o código e configuração do backend ficam em `app/` (Maven, Dockerfile, sources, resources, Liquibase).
- **Frontend:** Todo o código do frontend fica em `ui/` (Vite, package.json, src, assets).
- **Raiz:** Arquivos compartilhados: `.env.example`, `docker-compose.yml`, `README.md`, `AGENTS.md`, `CHANGELOG.md`, `adrs/`, `k3d/`, `.gitignore`, e (opcional) `package.json` para scripts de lint de documentação.

A API é consumida pelo front via proxy em desenvolvimento e, em produção/Compose, pelo mesmo host (path `/api`), sem necessidade de `VITE_API_BASE_URL` quando UI e API estão no mesmo origem.

## Consequências

- Build do backend: `cd app && mvn …`; build do front: `cd ui && npm run build`.
- Docker Compose inclui serviço de proxy (nginx) que serve a UI em `/` e a API em `/api` no mesmo host/porta (ver ADR 0008). Imagem da UI buildada em `ui/Dockerfile` (Nginx servindo o output do Vite).
