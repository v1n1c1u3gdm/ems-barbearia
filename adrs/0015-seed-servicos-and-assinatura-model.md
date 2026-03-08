# ADR 0015 – Seed dos serviços EMS e modelo Assinatura

## Status

Aceito.

## Contexto

A EMS Barbearia oferece um catálogo fixo de serviços (Corte, Barba, Sobrancelha, Barboterapia,
Hidratação, Alisante, Pigmentação, Luzes, Platinado). É desejável que esses serviços existam por
padrão no banco, com descrição e duração, e que a landing page os exiba dinamicamente. Além disso,
é necessário um modelo para vincular um cliente a um conjunto de serviços (assinatura): um cliente
pode ter uma ou mais assinaturas, cada uma com um ou mais serviços.

## Decisão

- **Seed de serviços (Liquibase 013):** Changeset que insere os 9 serviços com título, descrição e `duracao_minutos`, executado apenas quando a tabela `servico` está vazia (`preConditions onFail="MARK_RAN"` com `rowCount expectedRows="0"`). Descrições em português para cada serviço; durações estimadas (ex.: Corte 45 min, Platinado 120 min).
- **Landing page dinâmica:** A seção "Nossos serviços" deixa de usar lista estática e passa a chamar `fetchPublicServicos()` (GET /api/servicos) via TanStack Query; exibe loading e erro; texto sobre preços variando conforme procedimento/tamanho de cabelo mantido.
- **Modelo Assinatura:** Tabela `assinatura` (id, cliente_id FK, created_at) e tabela de junção `assinatura_servico` (assinatura_id, servico_id) para relação N:M. Uma assinatura pertence a um único cliente (N:1) e contém um ou mais serviços. CRUD admin em `/admin/assinaturas`; frontend com página de listagem, filtro por cliente, e modais para criar/editar (seleção de cliente e de serviços).

## Consequências

- Novos ambientes recebem os 9 serviços automaticamente; ambientes já populados não têm o seed reaplicado.
- A landing passa a depender da API; em caso de falha, é exibida mensagem de erro ao usuário.
- Assinatura permite ao admin configurar quais serviços um cliente tem direito (ex.: pacotes ou planos); não há lógica de cobrança ou vigência neste ADR.
