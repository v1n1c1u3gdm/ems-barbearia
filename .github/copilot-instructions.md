# Instruções para assistentes de código (GitHub Copilot)

Este repositório usa um único documento de regras para todos os assistentes: **AGENTS.md** na raiz.

## Obrigatório

- Antes de considerar qualquer implementação concluída, **siga o AGENTS.md** e **execute o checklist de verificação** descrito nele.
- Comandos do checklist:
  - Backend: `cd app && mvn verify` e `mvn validate`.
  - Frontend: `cd ui && npm run test` e `npm run lint`.
  - Containers: `docker compose down && docker compose up --build -d`.
  - Documentação: `npm run lint:docs` (na raiz) se houve alteração em Markdown.
- Testes no padrão AAA; cobertura mínima 80% quando aplicável.
- Não faça commit nem push sem solicitação explícita.

Leia o **AGENTS.md** na raiz para as regras completas.
