# EMS Barbearia

Projeto full-stack: backend Java/Spring Boot em `app/`, frontend React em `ui/`, desenvolvimento local com Docker Compose e deploy simulado com k3d.

## Pré-requisitos

- Docker e Docker Compose
- Node.js 18+ e npm (para rodar o front localmente)
- (Opcional) k3d e kubectl para ambiente Kubernetes local

## Desenvolvimento local com Docker Compose

1. Copie o arquivo de ambiente:

   ```bash
   cp .env.example .env
   ```

2. Ajuste as variáveis em `.env` se necessário.
3. Suba os serviços:

   ```bash
   docker compose up -d
   ```

4. Acesse em **http://localhost** (porta 80):
   - **UI:** `http://localhost/`
   - **API:** `http://localhost/api/` (ex.: `http://localhost/api/examples`)
   - **Documentação da API:** `http://localhost/api/docs.html` (`/api/docs` redireciona para o mesmo)

5. Para rodar o frontend em modo dev (com proxy para a API):

   ```bash
   cd ui && npm install && npm run dev
   ```

   Acesse `http://localhost:5173` (em dev a API continua em `http://localhost:8080` se o backend estiver rodando fora do Compose).

## Deploy local com k3d

Para simular deploys em um cluster Kubernetes local:

1. Consulte o [README em `k3d/`](k3d/README.md) para pré-requisitos e comandos.
2. Crie o cluster e aplique os manifests conforme documentado em `k3d/`.

## Estrutura do repositório

- `app/` – Backend Spring Boot (Maven, SpringDoc, Liquibase, MariaDB)
- `ui/` – Frontend React (Vite, Tailwind, TanStack Query, React Router, React Hook Form, Zod)
- `k3d/` – Scripts e manifests para cluster k3d
