# ADR 0017 – Disponibilidade por staff e seed Emerson e Renan Gabriel

## Status

Aceito.

## Contexto

Cada profissional (staff) deve ter sua própria disponibilidade para aparecer nos agendamentos: a API
pública e o admin precisam saber em quais dias e horários cada um atende. Além disso, é desejável um
seed inicial de dois profissionais (Emerson e Renan Gabriel) com disponibilidade padrão.

## Decisão

- **Tabela staff_disponibilidade (migração 016):** PK (staff_id, dia_semana); campos `aberto`,
  `hora_inicio`, `hora_fim` (mesmo padrão de horario_funcionamento: 0=dom a 6=sáb). FK para `staff`.
- **Seed de staff:** Inserção de dois registros em `staff` (Emerson, Renan Gabriel), ativos, apenas
  quando a tabela está vazia. Em seguida, inserção de 7 linhas por staff em `staff_disponibilidade`:
  domingo e segunda fechados; terça a sexta 09:00–19:00; sábado 09:00–16:00.
- **API Staff:** `StaffResponse` passa a incluir `horarios` (lista de diaSemana, aberto, horaInicio,
  horaFim). Endpoints que retornam staff (admin e API pública) carregam e expõem a disponibilidade.
- **Frontend:** Tipo `StaffResponse` com `horarios`; na página pública `/agendar`, ao selecionar um
  profissional, exibe texto "Disponível: …" com os dias e horários daquele staff (ex.: "Ter 09:00–19:00;
  Qua 09:00–19:00; …").

## Consequências

- Novos ambientes recebem Emerson e Renan Gabriel com disponibilidade padrão; ambientes já com staff
  não têm o seed de staff reaplicado. Disponibilidade pode ser estendida no futuro com CRUD admin.
  Entidades HorarioFuncionamento e StaffDisponibilidade usam `columnDefinition = "TINYINT"` para
  `dia_semana` para compatibilidade com o schema MariaDB (TINYINT).
