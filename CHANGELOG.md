# Changelog

Todas as mudanças notáveis neste projeto serão documentadas neste arquivo.

O formato é baseado em [Keep a Changelog](https://keepachangelog.com/pt-BR/1.0.0/),
e este projeto adere ao [Semantic Versioning](https://semver.org/lang/pt-BR/).

## [Unreleased]

### Added

- (Alterações ainda não liberadas.)

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
