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

4. Acesse em <http://localhost> (porta 80):
   - **UI:** <http://localhost/>
   - **API:** <http://localhost/api/> (ex.: <http://localhost/api/docs.html>)
   - **Documentação da API:** <http://localhost/api/docs.html> (`/api/docs` redireciona para o mesmo)

5. Para rodar o frontend em modo dev (com proxy para a API):

   ```bash
   cd ui && npm install && npm run dev
   ```

   Acesse <http://localhost:5173> (em dev a API continua em <http://localhost:8080> se o backend estiver rodando fora do Compose).

## Agendamento público

- **URL:** `/agendar`. O usuário identifica-se (Google, Apple, telefone/OTP ou conta com email/senha) e em seguida preenche serviço, profissional, data/hora e tipo para solicitar o agendamento (status PENDENTE).
- **Variáveis de ambiente (backend)** para auth pública e OAuth/OTP:
  - `JWT_PUBLIC_SECRET` – segredo para assinatura do JWT do cliente (obrigatório em produção).
  - `JWT_PUBLIC_EXPIRATION_MS` – validade do token em ms (ex.: 86400000 = 24h).
  - `PUBLIC_AUTH_FRONTEND_URL` – URL do frontend para redirect após OAuth (ex.: `http://localhost:5173`).
  - `PUBLIC_AUTH_BACKEND_BASE_URL` – URL base da API para callbacks OAuth (ex.: `http://localhost:8080/api`).
  - **Google:** `GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET` (OAuth 2.0 no Google Cloud Console).
  - **Apple:** `APPLE_CLIENT_ID`, `APPLE_TEAM_ID`, `APPLE_KEY_ID`, `APPLE_PRIVATE_KEY` (Sign in with Apple; chave .p8 em uma linha ou base64).
  - OTP: por padrão o código é apenas logado (dev). Para SMS/WhatsApp, configure um bean `OtpSender` (ex.: Twilio).

## Área administrativa

- **URL:** `/admin` (login em `/admin/login`).
- **Credenciais padrão:** usuário `admin`, senha `password`; ou usuário `adm-alessandra`, senha `Emerson@123` (altere em produção).
- **Painel:** após o login, o dashboard em `/admin` exibe totais de Contatos, Clientes, Agendamentos e Promoções, com links para cada área.
- **Áreas:** Contatos (`/admin/contatos`), Promoções (`/admin/promocoes`), Agendamentos (`/admin/agendamentos`), Clientes (`/admin/clientes`).

## Testes

### Unitários

- **Backend:** `cd app && mvn verify` — testes com JaCoCo; cobertura mínima 80%. Relatório: `app/target/site/jacoco/index.html`.
- **Frontend:** `cd ui && npm run test` ou `npm run test:coverage` — Vitest + React Testing Library; cobertura mínima 80%. Relatório: `ui/coverage/index.html`.

### E2E (Playwright)

Os testes E2E ficam em `tests/` e rodam contra o frontend em modo dev. Suba o front antes (`cd ui && npm run dev`) e em outro terminal:

```bash
cd tests && npm install && npm test
```

Opções: `npm run test:ui` (interface), `npm run test:headed` (browser visível), `npm run test:debug` (debug). Cada execução grava vídeo (WebM); o relatório HTML referencia os vídeos em `playwright-report/data/`. Para ver o relatório **com os vídeos**, abra via servidor: `cd tests && npm run report` (ou `npx playwright show-report`). Abrir só o `index.html` no navegador pode não carregar os vídeos.

## Deploy local com k3d

Para simular deploys em um cluster Kubernetes local:

1. Consulte o [README em `k3d/`](k3d/README.md) para pré-requisitos e comandos.
2. Crie o cluster e aplique os manifests conforme documentado em `k3d/`.

## Estrutura do repositório

- `app/` – Backend Spring Boot (Maven, SpringDoc, Liquibase, MariaDB)
- `ui/` – Frontend React (Vite, Tailwind, TanStack Query, React Router, React Hook Form, Zod)
- `tests/` – Testes E2E com Playwright (navegação e interação na UI)
- `adrs/` – Architecture Decision Records (decisões de arquitetura)
- `k3d/` – Scripts e manifests para cluster k3d
