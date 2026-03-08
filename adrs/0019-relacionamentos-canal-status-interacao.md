# ADR 0019 – Relacionamentos: canal, status e tipo de interação

## Status

Aceito.

## Contexto

A área admin de "Contatos" deve ser refatorada para "Relacionamentos" (com o cliente), com UI inspirada nos canais do Chatwoot (agrupamento por WhatsApp, E-mail, Instagram), API de leitura para uso futuro (ex.: n8n) e controle básico de status. É necessário definir o modelo de dados e as regras de status e interação.

## Decisão

- **Relacionamento:** Substitui o conceito de "Contato" na área admin. Representa o vínculo/comunicação com a pessoa (lead ou cliente). Campos principais:
  - Identificação: nome, email, telefone (obrigatórios para leads; quando houver `cliente_id`, podem ser herdados do Cliente).
  - **Canal:** enum `WHATSAPP` | `EMAIL` | `INSTAGRAM` (um por relacionamento; usado para agrupar na UI).
  - **Status:** um por relacionamento — `QUENTE` (leads que buscaram informações e/ou ainda não foram respondidos), `MORNO` (até 28 dias sem resposta/interação), `FRIO` (29+ dias sem interação), `GELADO` (60+ dias sem interação).
  - **Data da última interação** (`data_ultima_interacao`): timestamp; base para cálculo de status (Morno/Frio/Gelado) quando for automatizado.
  - **Tipo de interação:** `MOTIVADA_PELO_CLIENTE` (ex.: formulário, WhatsApp) ou `MOTIVADA_PELO_SISTEMA` (ex.: cadastro por indicação).
- **Persistência:** Nova tabela `relacionamento`; dados existentes em `contato` podem ser migrados para `relacionamento` (canal EMAIL, status QUENTE, tipo CLIENTE) e a tabela/entidade Contato removida ou mantida só leitura conforme changelog.
- **API:** Recursos de relacionamentos servidos por API (leitura; escrita de status/data/tipo para n8n e uso futuro). Primeira entrega na UI: apenas leitura + alteração de **status** (a interface não cria relacionamento nem edita data última interação ou tipo de interação).

## Consequências

- Backend expõe CRUD de relacionamentos e endpoint para atualização de status (e demais campos para integrações). Frontend na primeira entrega lista por canal, exibe detalhe e permite apenas alterar status.
- Histórico de consumo do cliente fica a um clique via agendamentos do cliente (ex.: `GET /admin/agendamentos?clienteId=`).
