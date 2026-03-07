# ADR 0009 – Estratégia de testes

## Status

Aceito.

## Contexto

O projeto precisa de testes automatizados para garantir regressão e qualidade, com meta de cobertura definida no AGENTS.md. É necessário definir onde e como rodar testes unitários (backend e frontend) e testes E2E.

## Decisão

- **Cobertura mínima:** 80% para testes unitários (backend e frontend), conforme regra obrigatória no AGENTS.md. Relatórios gerados no build (backend: JaCoCo; frontend: Vitest com provider v8).
- **Backend (`app/`):** JUnit 5 + MockMvc (`@WebMvcTest`); JaCoCo no Maven com `mvn verify`; relatório em `app/target/site/jacoco/index.html`. Exclusões de cobertura: classe principal e pacote `config/`.
- **Frontend (`ui/`):** Vitest + React Testing Library + jsdom; scripts `npm run test` e `npm run test:coverage`; relatório em `ui/coverage/`. Exclusões: entry, routes, config, lib, features e páginas não cobertas pelo núcleo de testes.
- **E2E:** Playwright em pasta `tests/` na raiz, com `package.json` e `playwright.config.ts` próprios; testes contra o frontend em modo dev (`http://localhost:5173`). Comando: `cd tests && npm install && npm test`; relatório HTML e vídeo por execução.
- **Padrão de testes:** Arrange-Act-Assert (AAA); AssertJ no backend quando aplicável; sem comentários explicativos dentro dos testes (AGENTS.md).

## Consequências

- CI ou desenvolvedor deve rodar `mvn verify` no backend e `npm run test` / `npm run test:coverage` no frontend para validar cobertura.
- E2E exige frontend em execução (`cd ui && npm run dev`) antes de rodar os testes em `tests/`.
- Nova funcionalidade deve vir acompanhada de testes unitários e, quando relevante, de cenários E2E.
