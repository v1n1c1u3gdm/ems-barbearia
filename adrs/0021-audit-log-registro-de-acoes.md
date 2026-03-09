# ADR 0021 – Audit log: registro de ações no banco

## Status

Aceito.

## Contexto

É necessário registrar no sistema cada ação relevante (criação, alteração, exclusão, fluxos de autenticação) para rastreabilidade e auditoria. O registro deve ser persistido em banco, com identificação de quem solicitou a ação e, quando aplicável, estado anterior e posterior.

## Decisão

- **Tabela `audit_log`:** Campos `id`, `data_hora`, `acao`, `estado_anterior`, `estado_posterior`, `solicitante`, `metodo_http`, `path`, `status_http`. Migração Liquibase 024. Estados em texto (JSON) com limite de tamanho para evitar overflow; sem sanitização de conteúdo (registro em nível de domínio).
- **Solicitante:** Obtido do contexto de segurança: `"publico"` quando anônimo; `"cliente:{id}"` para usuário público autenticado; `"admin:{nome}"` para admin. Nunca propaga exceção no `log()` para não impactar o fluxo principal.
- **Filtro HTTP (AuditFilter):** `OncePerRequestFilter` que registra apenas requisições GET, HEAD e OPTIONS (leitura); exclui caminhos `/actuator/`, `/v3/api-docs`, `/swagger-ui`. Registrado após o filtro JWT público na cadeia de segurança.
- **Serviços de domínio:** Agendamento, Cliente, Servico, Staff, Assinatura, Relacionamento, PublicAuth, Auth e Otp chamam `AuditLogService.log()` em create/update/delete e nos fluxos de autenticação relevantes.

## Consequências

- Toda ação relevante fica registrada em `audit_log` com data/hora, ação, solicitante e, quando aplicável, estado anterior/posterior e dados da requisição HTTP.
- Consultas e health/docs não poluem o log de ações de escrita; leituras (GET/HEAD/OPTIONS) são registradas pelo filtro quando não excluídas.
- Novos serviços ou operações que precisem de auditoria devem invocar `AuditLogService.log()` no ponto apropriado.
