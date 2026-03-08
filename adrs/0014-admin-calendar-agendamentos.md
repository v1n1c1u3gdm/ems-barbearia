# ADR 0014 – Painel admin: calendário de agendamentos

## Status

Aceito.

## Contexto

O admin precisa visualizar os agendamentos em formato de calendário (dia/semana) para facilitar a gestão e aprovar solicitações pendentes. A tela deve exibir blocos por horário, indicar a hora atual e permitir aprovar com um clique.

## Decisão

- **Vista:** Seletor Dia ou Semana; navegação por data (anterior/próxima). Semana inicia na segunda-feira.
- **Grade:** Colunas = dias (1 ou 7); linhas = intervalos de 30 min entre 6h e 22h. Cada agendamento no período é exibido como bloco posicionado no dia e horário corretos (dataHora a dataHoraFim). Cores por status (ex.: PENDENTE amarelo, APROVADO verde).
- **Indicador de hora atual:** Linha horizontal na coluna do dia atual na posição da hora atual.
- **Modal:** Ao clicar em um bloco, abre modal com dados em somente leitura (cliente, serviço, staff, data/hora, tipo, status) e botão "Aprovar" (chama PATCH status APROVADO). Modal fecha e a lista é invalidada após sucesso.
- **Dados:** Frontend usa `GET /admin/agendamentos?de=ISO&ate=ISO` (TanStack Query) para o intervalo visível; mutação de status invalida a query.

## Consequências

- Substituição da tela "Em breve" pela grade de calendário. Implementação inspirada em abordagens de calendários (ex.: grade por dia/semana, blocos absolutos). Sem dependência de biblioteca externa de calendário; layout com Tailwind e estado local para data e vista.
- A grade e o slot passaram a ser configuráveis conforme ADR 0016 (configuração da agenda: slot global e horário de funcionamento por dia).
