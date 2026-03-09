# Changelog

Todas as mudanças notáveis neste projeto serão documentadas neste arquivo.

O formato é baseado em [Keep a Changelog](https://keepachangelog.com/pt-BR/1.0.0/),
e este projeto adere ao [Semantic Versioning](https://semver.org/lang/pt-BR/).

## [Unreleased]

### Added

- **Audit log (ADR 0021):** Registro de ações no banco: tabela `audit_log` (data_hora, acao, estado_anterior,
  estado_posterior, solicitante, metodo_http, path, status_http). Migração Liquibase 024. `AuditLogService` com
  solicitante do contexto (`"publico"` / `"cliente:{id}"` / `"admin:{nome}"`); `log()` não propaga exceção.
  `AuditFilter` (GET/HEAD/OPTIONS) registra leituras; exclusão de actuator e Swagger. Serviços de domínio
  (Agendamento, Cliente, Servico, Staff, Assinatura, Relacionamento, PublicAuth, Auth, Otp) chamam o serviço em
  create/update/delete e fluxos de auth.
- **Configuração da agenda (ADR 0016):** Tabelas `configuracao_agenda` (slot_minutos default 30) e
  `horario_funcionamento` (dia_semana 0–6, aberto, hora_inicio, hora_fim). Migração Liquibase 015 com
  dados iniciais (dom/seg fechado; ter–sex 09:00–19:00; sáb 09:00–16:00). Endpoint
  `GET /admin/configuracao-agenda`. Calendário admin passa a usar slot e horário por dia: colunas
  "Fechado" ou grade entre hora_inicio e hora_fim; posicionamento de blocos e linha da hora atual
  calculados por dia.
- **Disponibilidade por staff e seed (ADR 0017):** Tabela `staff_disponibilidade` (staff_id,
  dia_semana, aberto, hora_inicio, hora_fim). Migração 016 com seed de dois staff (Emerson, Renan
  Gabriel) e respectiva disponibilidade padrão (ter–sex 09–19, sáb 09–16). `StaffResponse` inclui
  `horarios`; API admin e pública retornam disponibilidade. Página `/agendar` exibe "Disponível: …"
  para o profissional selecionado.
- **Ícones nas ações das tabelas (ADR 0018):** Biblioteca `lucide-react` no frontend; ações Ver,
  Editar e Excluir nas tabelas admin (Clientes, Contatos, Serviços, Staff, Assinaturas) passam a
  usar ícones (Eye, Pencil, Trash2); botões "Novo X" ganham ícone Plus. Uso de `title` e
  `aria-label` para acessibilidade.
- Refatoração do domínio **Promoção → Serviço**: entidade `Servico` com `duracaoMinutos`; CRUD em `/admin/servicos`; dashboard e frontend atualizados (AdminServicosPage, rotas, menu). Migração Liquibase 010.
- Entidade **Staff** (profissional/barbeiro): CRUD em `/admin/staff`; migração Liquibase 011; página admin `AdminStaffPage` com DataTable e modais.
- **Agendamento** com tipo FIRME/ENCAIXE, FKs para Serviço e Staff, `dataHoraFim` calculada pela duração do serviço; validação de sobreposição apenas para tipo FIRME (mesmo staff). Migração Liquibase 012. Endpoints admin: list com filtros e intervalo `de`/`ate`, `PATCH /admin/agendamentos/{id}/status` para aprovar.
- **API pública** de agendamento: `GET /api/servicos`, `GET /api/staff` (ativos), `POST /api/agendamentos` (status PENDENTE). Autenticação pública com JWT (login/cadastro por email, OTP por telefone, OAuth Google/Apple); tratamento de erros de configuração OAuth no backend e exibição de mensagens via query params na página de agendamento.
- **Tela pública `/agendar`**: gate "Entrar ou cadastrar" (Google, Apple, telefone/OTP, criar conta com email); após autenticação, formulário para serviço, staff, data/hora e tipo (Firme/Encaixe); submit cria agendamento PENDENTE; link no header público.
- **Painel admin – calendário de agendamentos**: vista Dia/Semana, grade 6h–22h, blocos por agendamento (cores por status), indicador de hora atual; modal ao clicar no bloco com detalhes e botão Aprovar.
- **Seed dos serviços EMS** (migração 013): inserção inicial dos 9 serviços (Corte, Barba, Sobrancelha, Barboterapia, Hidratação, Alisante, Pigmentação, Luzes, Platinado) com descrição e duração; executa apenas quando a tabela `servico` está vazia.
- **Landing page – serviços dinâmicos**: seção "Nossos serviços" passa a consumir `GET /api/servicos` (TanStack Query); exibição dos serviços ativos com descrição; estados de loading e erro.
- **Modelo Assinatura**: uma assinatura pertence a um cliente e agrupa um ou mais serviços; tabelas `assinatura` e `assinatura_servico` (migração 014); CRUD em `/admin/assinaturas`; página admin `AdminAssinaturasPage` com DataTable e modais (Ver, Editar, Excluir, Nova assinatura).
- Testes E2E com Playwright em `tests/`: navegação (home, link Agendar, scroll Início/Serviços/Contato), proteção admin (`/admin`, `/admin/servicos`, `/admin/clientes` → login), página `/agendar` (gate de auth), login admin (erro e sucesso com `E2E_ADMIN_USER`/`E2E_ADMIN_PASSWORD`), smoke do footer; relatório HTML e vídeo; base `http://localhost:5173`; README com backend e credenciais E2E.
- Testes unitários no backend: JaCoCo com cobertura mínima 80% (exclusões: aplicação principal e `config/`); testes para `HelloController`, `AuthController`, `ExampleController` (`@WebMvcTest`).
- Testes unitários no frontend: Vitest + React Testing Library + jsdom; cobertura mínima 80% (exclusões: entry, routes, config, lib, features e páginas não cobertas); testes para layout (PublicHeader, PublicFooter, PageLayout, AdminLayout), ProtectedAdminRoute, Button, LandingPage.
- Link do Instagram no footer (ícone SVG) apontando para o perfil da barbearia; endereço completo e link no rodapé.
- Config de contato: `fullAddress` e `instagramUrl` em `ui/src/config/contact.ts`.
- **Provérbio bíblico aleatório**: endpoint na API pública; tabela e seed de provérbios (referências em português); footer exibe provérbio aleatório (TanStack Query).
- Lint de documentação: script `npm run lint:docs` passa a incluir `CLAUDE.md` e arquivos em `.github/`.

### Changed

- **Cobertura de testes frontend:** Meta de 80% em statements, branches, functions e lines mantida; exclusão de `**/*.test.{ts,tsx}` e `**/*.spec.{ts,tsx}` do cálculo no Vitest.
  Novos ou ampliados testes para Modal (clique no backdrop), PageLayout (redirect com hash em `/agendar`), PublicFooter (provérbio e copyright ano 2024), AdminLayout (nav pública na login e link ativo em `/admin/agendamentos`), LandingPage (loading, erro, scroll por hash, descrição padrão de serviço), AgendarPage (validação, erro de API, modo Dia, tipo Encaixe, disponibilidade e “Fechado” ).

- Dashboard administrativo: total de "Promoções" substituído por "Serviços" (campo `servicos` no summary); área "Contatos" substituída por "Relacionamentos" (`/admin/relacionamentos`). AGENTS.md com regras de cobertura obrigatória e migrações de banco.
- Agendamento: modelo passa a usar `servico_id` e `staff_id`; coluna legada `servico` (string) removida.
- Seção de contato da home: endereço removido da seção (mantidos título, texto, botões e mapa).
- Footer: endereço exibido como `fullAddress` (Rua Doutor Romeo Ferro, 612 — Jardim Bonfiglioli, São Paulo); link do Instagram substitui texto por ícone.

### Removed

- Domínio **Promoção**: entidade, repositório, service, controller e DTOs substituídos por Serviço.
- Link "Admin" no footer da home (acesso ao admin apenas por URL direta).

## [0.1.0] – 2025-03-07

### Added

- Inicialização do repositório Git e estrutura full-stack.
- Backend em `app/`: Java 21 (Amazon Corretto), Spring Boot 3.4, Maven, SpringDoc OpenAPI, Liquibase, MariaDB, Actuator (health).
- Frontend em `ui/`: React, Vite, TypeScript, Tailwind CSS v4, TanStack Query, React Router, React Hook Form, Zod; estrutura por features.
- Docker Compose para desenvolvimento local (app + MariaDB 10.11 LTS); credenciais via variáveis de ambiente.
- Ambiente k3d para simular deploy em Kubernetes (manifests e scripts em `k3d/`).
- Documentação: README.md, AGENTS.md (regras para assistentes), ADRs em `adrs/`, CHANGELOG.md.
- Linting de documentação com markdownlint-cli (configuração em `.markdownlint.json`, script `npm run lint:docs` na raiz).

[Unreleased]: https://github.com/viniciusmenezes/ems-barbearia/compare/v0.1.0...HEAD
[0.1.0]: https://github.com/viniciusmenezes/ems-barbearia/releases/tag/v0.1.0
