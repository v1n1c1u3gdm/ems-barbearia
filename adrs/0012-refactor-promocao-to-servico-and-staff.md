# ADR 0012 – Refatoração Promoção → Serviço e entidade Staff

## Status

Aceito.

## Contexto

O domínio "Promoção" foi renomeado para "Serviço" para refletir melhor o catálogo de serviços da barbearia (corte, barba, etc.). Cada serviço precisa de uma duração em minutos para cálculo de horário de fim e validação de sobreposição de agendamentos. Além disso, é necessário modelar o profissional que atende (barbeiro), referenciado pelos agendamentos.

## Decisão

- **Promoção → Serviço:** Tabela `promocao` renomeada para `servico` (Liquibase 010); entidade `Servico` com campos existentes (titulo, descricao, validoDe, validoAte, ativo, createdAt) mais `duracaoMinutos` (Integer, default 30). CRUD em `/admin/servicos`. Dashboard e frontend passam a usar "Serviços" e o campo `servicos` no summary.
- **Entidade Staff:** Nova tabela `staff` (Liquibase 011) com id, nome, ativo, created_at. CRUD em `/admin/staff`; página admin com DataTable (nome, ativo, ações). Staff representa o profissional que realiza o serviço no agendamento.

## Consequências

- Código e testes que referenciam Promoção foram removidos ou migrados para Servico. O dashboard (ADR 0011) passa a expor `servicos` em vez de `promocoes`.
- Serviço.duracaoMinutos é usado pelo modelo de Agendamento para calcular dataHoraFim e validar sobreposição (ADR 0013).
- Staff é referenciado por Agendamento (FK staff_id); listagens públicas (formulário de agendamento) usam apenas staff ativo.
