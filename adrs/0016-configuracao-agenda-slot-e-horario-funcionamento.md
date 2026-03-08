# ADR 0016 – Configuração da agenda: slot global e horário de funcionamento

## Status

Aceito.

## Contexto

O calendário de agendamentos (ADR 0014) precisa respeitar parâmetros configuráveis que afetam a
disponibilidade exibida: tamanho do slot (ex.: 30 min) e horário de funcionamento por dia da semana.
Assim, dias fechados ou com horários diferentes (ex.: sábado até 16h) passam a ser refletidos na grade.

## Decisão

- **Configuração global (migração 015):** Tabela `configuracao_agenda` com `slot_minutos` (default 30).
  Tabela `horario_funcionamento` com `dia_semana` (0=domingo a 6=sábado), `aberto` (boolean),
  `hora_inicio` e `hora_fim` (TIME). Dados iniciais: domingo e segunda fechados; terça a sexta 09:00–19:00;
  sábado 09:00–16:00.
- **API:** `GET /admin/configuracao-agenda` retorna `slotMinutos` e lista de `horarios` (diaSemana, aberto,
  horaInicio, horaFim) ordenada por dia.
- **Calendário admin:** Passa a consumir essa config (TanStack Query). Por dia da coluna: se `!aberto`,
  exibe "Fechado" sem grade; se `aberto`, exibe apenas as faixas entre `horaInicio` e `horaFim` com slots
  de `slotMinutos`. Altura da linha fixa (ex.: 40px); posicionamento de blocos e linha da hora atual
  calculados com base no horário do dia.

## Consequências

- Backend: entidades ConfiguracaoAgenda e HorarioFuncionamento, repositórios, ConfiguracaoAgendaService,
  ConfiguracaoAgendaController. Frontend: fetchConfiguracaoAgenda; AdminAgendamentosPage usa slot e
  horário por dia. Calendário deixa de usar constantes fixas (6h–22h, 30 min) e passa a depender da config.
