# ADR 0020 – Agendamento público com auth (OAuth, OTP, conta própria)

## Status

Aceito.

## Contexto

A jornada pública de agendamento (`/agendar`) precisa identificar o cliente antes de criar o agendamento. É requisito permitir login/cadastro por Google, Apple, telefone (OTP por SMS/WhatsApp) e conta própria (email/senha).

## Decisão

- **Credenciais:** Tabela `cliente_credential` (cliente_id, provider, external_id, password_hash opcional) com unique (provider, external_id). Providers: EMAIL, GOOGLE, APPLE, PHONE.
- **JWT público:** Emitido após qualquer fluxo de auth do cliente; payload com `sub` = id do Cliente e `typ: "public"`. Rotas `GET /auth/public/me` e `POST /agendamentos` exigem Bearer JWT público.
- **Endpoints:** `POST /auth/public/register`, `POST /auth/public/login`, `GET /auth/public/me`; redirect/callback para Google e Apple OAuth; `POST /auth/public/phone/request-otp`, `POST /auth/public/phone/verify-otp`. OTP com rate limit (1 req/min por telefone) e TTL de 5 min.
- **Agendamento:** Body de `POST /agendamentos` sem clienteId; backend preenche cliente a partir do JWT.
- **Frontend:** Em `/agendar`, sem token exibe tela “Entrar ou cadastrar” (Google, Apple, telefone, criar conta, login). Com token exibe formulário de agendamento (serviço, staff, data/hora, tipo). Token armazenado em sessionStorage; redirect OAuth devolve `?token=...` na URL.

## Consequências

- Admin e cliente usam mecanismos de auth separados (token admin vs JWT público).
- OTP em produção exige implementação de `OtpSender` (ex.: Twilio). Em dev, código é logado.
- Google/Apple exigem configuração em cada provedor e variáveis de ambiente no backend.
