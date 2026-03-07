# ADR 0004 – Migrações de schema: Liquibase

## Status

Aceito.

## Contexto

O schema do banco deve evoluir de forma versionada, reproduzível e aplicável em todos os ambientes (dev, k3d, futuros). JPA com `ddl-auto=validate` evita alterações automáticas em produção.

## Decisão

- **Ferramenta:** Liquibase, integrado ao Spring Boot.
- **Arquivo mestre:** `app/src/main/resources/db/changelog/db.changelog-master.xml`.
- **Changelogs:** Inclusões em `db/changelog/changes/` (ex.: `001-create-example-table.xml`).
- **Configuração:** `spring.liquibase.change-log` apontando para o master; JPA com `ddl-auto: validate`.

## Consequências

- Histórico de alterações de schema versionado no repositório.
- Mesmo conjunto de mudanças aplicado em qualquer ambiente na subida da aplicação.
- Evita drift de schema entre ambientes.
