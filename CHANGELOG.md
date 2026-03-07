# Changelog

Todas as mudanças notáveis neste projeto serão documentadas neste arquivo.

O formato é baseado em [Keep a Changelog](https://keepachangelog.com/pt-BR/1.0.0/),
e este projeto adere ao [Semantic Versioning](https://semver.org/lang/pt-BR/).

## [Unreleased]

### Added

- Testes E2E com Playwright em `tests/`: navegação na home, links do header/footer, rotas diretas (`/examples`, `/admin`); relatório HTML e gravação de vídeo; base URL `http://localhost:5173` (front em dev).
- Testes unitários no backend: JaCoCo com cobertura mínima 80% (exclusões: aplicação principal e `config/`); testes para `HelloController`, `AuthController`, `ExampleController` (`@WebMvcTest`).
- Testes unitários no frontend: Vitest + React Testing Library + jsdom; cobertura mínima 80% (exclusões: entry, routes, config, lib, features e páginas não cobertas); testes para layout (PublicHeader, PublicFooter, PageLayout, AdminLayout), ProtectedAdminRoute, Button, LandingPage.
- Link do Instagram no footer (ícone SVG) apontando para o perfil da barbearia; endereço completo e link no rodapé.
- Config de contato: `fullAddress` e `instagramUrl` em `ui/src/config/contact.ts`.

### Changed

- Seção de contato da home: endereço removido da seção (mantidos título, texto, botões e mapa).
- Footer: endereço exibido como `fullAddress` (Rua Doutor Romeo Ferro, 612 — Jardim Bonfiglioli, São Paulo); link do Instagram substitui texto por ícone.

### Removed

- Link "Admin" no footer da home (acesso ao admin apenas por URL direta).

## [0.1.0] – 2025-03-07

### Added

- Inicialização do repositório Git e estrutura full-stack.
- Backend em `app/`: Java 21 (Amazon Corretto), Spring Boot 3.4, Maven, SpringDoc OpenAPI, Liquibase, MariaDB, Actuator (health).
- Frontend em `ui/`: React, Vite, TypeScript, Tailwind CSS v4, TanStack Query, React Router, React Hook Form, Zod; estrutura por features.
- Docker Compose para desenvolvimento local (app + MariaDB 10.11 LTS); credenciais via variáveis de ambiente.
- Ambiente k3d para simular deploy em Kubernetes (manifests e scripts em `k3d/`).
- Documentação: README.md, AGENTS.md (regras para assistentes), ADRs em `adrs/`, CHANGELOG.md.
- Linting de documentação com markdownlint-cli (configuração em `.markdownlint.json`, script `npm run lint:docs` na raiz).

[Unreleased]: https://github.com/viniciusmenezes/ems-barbearia/compare/v0.1.0...HEAD
[0.1.0]: https://github.com/viniciusmenezes/ems-barbearia/releases/tag/v0.1.0
