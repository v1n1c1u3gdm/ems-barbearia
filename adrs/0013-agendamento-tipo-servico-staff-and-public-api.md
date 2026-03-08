# ADR 0013 – Agendamento com tipo, serviço, staff e API pública

## Status

Aceito.

## Contexto

Os agendamentos precisam estar vinculados a um serviço (com duração) e a um profissional (staff). Deve ser possível diferenciar agendamentos "firmes" (horário fixo, não podem sobrepor no mesmo staff) de "encaixes" (podem sobrepor). O cliente deve poder solicitar agendamento pela web sem login, com aprovação posterior pelo admin.

## Decisão

- **Modelo Agendamento:** Campos cliente (FK), dataHora, dataHoraFim (calculado: dataHora + Servico.duracaoMinutos), servico (FK), staff (FK), tipo (FIRME | ENCAIXE), status (PENDENTE, APROVADO, CANCELADO, REALIZADO). Coluna legada `servico` (string) removida. Migração Liquibase 012.
- **Regra FIRME:** Ao criar/atualizar com tipo FIRME, validar que não existe outro agendamento FIRME do mesmo staff com intervalo [dataHora, dataHoraFim] sobreposto; em conflito retornar 409.
- **Admin:** CRUD em `/admin/agendamentos`; list com filtros opcionais (clienteId, staffId, status) e intervalo de datas (de, ate) para o calendário; `PATCH /admin/agendamentos/{id}/status` para aprovar/rejeitar.
- **API pública (sem auth):** `GET /api/servicos` e `GET /api/staff` retornam apenas ativos; `POST /api/agendamentos` recebe clienteId, servicoId, staffId, dataHora, tipo e define status PENDENTE; mesma validação de sobreposição para FIRME.

## Consequências

- Tela pública `/agendar` consome a API pública para listar serviços e staff e submeter o agendamento. Clientes são listados via endpoint admin (ou futuro endpoint público de clientes).
- Calendário admin (ADR 0014) usa o list com de/ate para exibir blocos por período. Aprovação via PATCH status.
