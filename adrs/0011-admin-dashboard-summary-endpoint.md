# ADR 0011 – Resumo do dashboard administrativo (endpoint único de totais)

## Status

Aceito.

## Contexto

O painel pós-login (`/admin`) deve exibir um resumo útil: totais de Contatos, Clientes, Agendamentos e Promoções, com links para cada área. É necessário definir como o frontend obtém esses totais.

## Decisão

- **Endpoint único:** `GET /admin/dashboard/summary` retorna um JSON com quatro campos numéricos: `contatos`, `clientes`, `agendamentos`, `promocoes`, correspondentes aos `count()` dos respectivos repositórios JPA.
- **DTO:** `DashboardSummaryResponse` (record) com os quatro campos; serviço `DashboardService` agrega os repositórios e expõe um único método; controller `DashboardController` sob `@RequestMapping("/admin/dashboard")`.
- **Frontend:** O dashboard usa TanStack Query para chamar esse endpoint uma vez; exibe os totais nos cards de cada área e mantém links para as rotas de listagem. Estados de loading e erro são tratados na UI.

## Consequências

- Uma única requisição HTTP para popular o dashboard, em vez de quatro chamadas às listagens.
- Alteração futura (ex.: novos totais ou filtros) pode exigir evolução do DTO e do service; o contrato do endpoint permanece estável enquanto os nomes dos campos não mudarem.
- O endpoint não exige autenticação na API (conforme ADR 0010); a proteção é apenas de rota no frontend.
