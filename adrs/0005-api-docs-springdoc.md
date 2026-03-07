# ADR 0005 – Documentação da API: SpringDoc OpenAPI

## Status

Aceito.

## Contexto

A API REST precisa de documentação interativa e contrato (OpenAPI) para consumo pelo frontend e por integrações. O ecossistema Spring tem suporte nativo a OpenAPI 3.

## Decisão

- **Biblioteca:** springdoc-openapi-starter-webmvc-ui (SpringDoc 2.x).
- **Endpoints (relativos ao context-path):** Especificação OpenAPI em `/v3/api-docs`, UI em `/docs.html`. Com `server.servlet.context-path=/api`, ficam em `/api/v3/api-docs` e `/api/docs.html`. O SpringDoc redireciona `/api/docs.html` para `/api/swagger-ui/index.html`; o URL público permanece `/api/docs.html`. Não se usa `swagger-ui.html` como URL de acesso.
- **Try it out:** `OpenApiConfig` define o servidor com `url="/api"` e em `application.yml` usa-se `springdoc.swagger-ui.url=/api/v3/api-docs` para que a Swagger UI envie as requisições de teste para o mesmo host e path.
- **Documentação:** Anotações nos controllers e DTOs (ex.: `@Tag`, `@Operation`, `@Schema`) para descrever recursos e payloads.
- **Acesso via proxy:** Documentação em `/api/docs.html`; `/api/docs` e `/api/doc.html` redirecionam para `/api/docs.html` (ver ADR 0008).

## Consequências

- Documentação sempre alinhada ao código; possível geração de clientes a partir do OpenAPI.
- Desenvolvimento e testes manuais facilitados pela Swagger UI. Um único host/porta serve UI, API e docs.
