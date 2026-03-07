# ADR 0002 – Frontend: React, Vite e stack associada

## Status

Aceito.

## Contexto

É necessário um frontend SPA que consuma a API REST, com boa DX, tipagem e padrões atuais. A aplicação será evoluída com formulários, listagens e rotas.

## Decisão

- **Framework:** React 18+ com TypeScript.
- **Build e dev server:** Vite.
- **Estilo:** Tailwind CSS (v4 com plugin @tailwindcss/vite).
- **Estado servidor e formulários:** TanStack Query (dados da API), React Hook Form com Zod (validação e formulários).
- **Rotas:** React Router v6+.

O frontend fica no diretório `ui/`, com estrutura por features em `src/` (components, features, hooks, lib, pages, routes, types).

## Consequências

- Desenvolvimento rápido com HMR e proxy para a API em dev.
- Código tipado e validação de formulários alinhada aos tipos (Zod).
- Separação clara entre UI reutilizável, features e configuração (lib, queryClient, api).
