# Instruções para assistentes de código (GitHub Copilot)

Este repositório usa um único documento de regras para todos os assistentes: **AGENTS.md** na raiz.

## Obrigatório

- Antes de considerar qualquer implementação concluída, **siga o AGENTS.md** e **execute o checklist de verificação** descrito nele.
- Comandos do checklist:
  - Backend: `cd app && mvn verify` (testes com cobertura JaCoCo) e `mvn validate`.
  - Frontend: `cd ui && npm run test:coverage` (testes com cobertura) e `npm run lint`.
  - Containers: `docker compose down && docker compose up --build -d`.
  - Migrações: se alterou `db/changelog/` ou entidades que impactam schema, conferir que os changesets estão em `db.changelog-master.xml` e que os containers foram recriados (Liquibase executa na subida do app).
  - Documentação: `npm run lint:docs` (na raiz) se houve alteração em Markdown.
- Testes no padrão AAA; cobertura mínima 80% quando aplicável.
- Não faça commit nem push sem solicitação explícita.

Leia o **AGENTS.md** na raiz para as regras completas.
