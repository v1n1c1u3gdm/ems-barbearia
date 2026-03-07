# AGENTS.md – Regras para assistentes de código (EMS Barbearia)

Este documento define regras obrigatórias para qualquer assistente (Cursor, Copilot, Cloud Code, etc.) ao trabalhar neste repositório. O projeto é full-stack: **backend** em `app/` (Java 21, Spring Boot, Maven, MariaDB, Liquibase) e **frontend** em `ui/` (React, Vite, TypeScript, Tailwind).

---

## REGRA OBRIGATÓRIA: Execução de testes com cobertura

**Após toda e qualquer implementação, execute os testes com cobertura antes de finalizar a tarefa.**

### Backend (app/)

```bash
cd app && mvn verify
```

- Use JaCoCo (ou ferramenta equivalente) para cobertura; o relatório deve ser gerado no build.
- **Cobertura mínima exigida: 80%.**
- Se testes falharem, corrija antes de continuar.
- Não pule esta etapa em nenhuma implementação.

### Frontend (ui/)

```bash
cd ui && npm run test
```

- Se houver script de testes (ex.: Vitest, React Testing Library), execute após alterações no front.
- Mantenha ou estabeleça cobertura mínima de 80% quando ferramenta de cobertura estiver configurada.

---

## REGRA OBRIGATÓRIA: Padrão Arrange-Act-Assert (AAA)

**Todos os testes devem seguir o padrão Arrange-Act-Assert (AAA), com seções claras.**

### Backend (Java/JUnit 5)

- **Arrange:** preparar dados, mocks e dependências.
- **Act:** executar o método ou fluxo sob teste.
- **Assert:** verificar resultado com assertions expressivas.

Preferir **AssertJ** para assertions fluentes quando disponível:

```java
// Arrange
var request = new ExampleRequest("Nome");
var entity = new ExampleEntity();
entity.setName(request.name());

// Act
var response = controller.create(request);

// Assert
assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
assertThat(response.getBody()).isNotNull();
assertThat(response.getBody().name()).isEqualTo("Nome");
```

- Um comportamento por teste; nomes de método descritivos (ex.: `create_shouldReturn201WhenValidRequest`).
- Não use comentários explicativos dentro do teste; o AAA e o nome do teste devem deixar claro o que está sendo validado.

### Frontend (React/TypeScript)

- **Arrange:** renderizar componente, definir props e estado necessário.
- **Act:** simular eventos ou chamadas (userEvent, mock de API).
- **Assert:** verificar resultado na UI ou em mocks (expect do Vitest/Jest).

---

## REGRA OBRIGATÓRIA: Recriação de containers

**Após modificar qualquer aspecto do código (app ou configuração de containers), os containers devem ser recriados.**

### Comando obrigatório

```bash
docker compose down
docker compose up --build -d
```

### Regras

1. Recrie os containers após qualquer mudança no código ou em Dockerfile/docker-compose.
2. Use `--build` para forçar rebuild da imagem do app.
3. Use `-d` para executar em background.
4. Verifique se os containers sobem corretamente (logs, health) antes de dar a tarefa por concluída.

---

## REGRA OBRIGATÓRIA: Documentação centralizada

**Toda documentação principal do projeto fica no `README.md` na raiz.**

### Regras

1. **Não crie arquivos de documentação separados** na raiz (ex.: CONTRIBUTING.md, ARCHITECTURE.md) a menos que seja explicitamente solicitado.
2. **Atualize o `README.md`** quando houver mudanças que precisem ser documentadas (setup, comandos, estrutura).
3. **Exceção:** o diretório `k3d/` pode ter seu próprio `README.md` para instruções específicas de uso do k3d (conforme planejado no projeto).
4. **Não duplique documentação:** uma única fonte de verdade no README principal.
5. **Documente apenas quando solicitado** – não adicione documentação proativamente sem pedido do usuário.
6. **ADRs** ficam em `adrs/`; **histórico de versões** em `CHANGELOG.md` (padrão Keep a Changelog).

---

## REGRA OBRIGATÓRIA: Linting de documentação

**Se houver alteração em arquivos Markdown, execute o linting de documentação antes de finalizar.**

### Comando

Na raiz do repositório (onde está o `package.json` de lint de docs):

```bash
npm install
npm run lint:docs
```

Ou com markdownlint-cli instalado globalmente:

```bash
markdownlint -c .markdownlint.json "README.md" "CHANGELOG.md" "AGENTS.md" "adrs/**/*.md" "k3d/README.md"
```

### Regras

1. **Execute após mudanças em .md** – Qualquer alteração em README, CHANGELOG, AGENTS.md, arquivos em `adrs/` ou `k3d/README.md` deve ser validada com o comando acima.
2. **Corrija erros reportados** – Ajuste formatação/estilo conforme `.markdownlint.json`.
3. **Configuração** – O arquivo `.markdownlint.json` na raiz define as regras (ex.: tamanho de linha, títulos, listas).

### Instalação

Na raiz: `npm install` (o `package.json` inclui `markdownlint-cli` como devDependency). Ou instale globalmente: `npm install -g markdownlint-cli`.

---

## REGRA OBRIGATÓRIA: Linting e formatação

**Após implementação, execute o lint/format do trecho que você alterou.**

### Backend (app/)

```bash
cd app && mvn validate
```

- Respeite o estilo configurado no projeto (Checkstyle/Spotless, se existir).
- Corrija avisos de compilação e de testes antes de finalizar.

### Frontend (ui/)

```bash
cd ui && npm run lint
```

- **Sempre use ESLint** com os plugins do projeto: **eslint-plugin-react** (regras React e JSX), **eslint-plugin-react-hooks** (Rules of Hooks), **eslint-plugin-react-refresh** (Vite HMR). A configuração está em `ui/eslint.config.js` (flat config).
- Execute `npm run lint` após qualquer alteração em `ui/src/` e corrija erros antes de finalizar.
- Não desabilite regras dos plugins React sem justificativa; mantenha o padrão recomendado (jsx-runtime, hooks, refresh).

---

## REGRA OBRIGATÓRIA: Sem scripts de helper

**Não crie scripts de helper ou código fora do fluxo da aplicação sem solicitação explícita.**

1. Não crie scripts de automação de desenvolvimento sem pedido.
2. Não crie utilitários, helpers ou ferramentas auxiliares fora do app/ui sem pedido.
3. Foque no código da aplicação (app/ e ui/).

---

## REGRA OBRIGATÓRIA: Sem commits ou push automáticos

**Nunca faça commit ou push sem ser solicitado explicitamente.**

1. Não execute `git commit` ou `git push` sem solicitação explícita.
2. Não sugira mensagens de commit sem ser pedido.
3. Pode usar `git add` quando fizer sentido, mas o versionamento é responsabilidade do usuário.

---

## REGRA OBRIGATÓRIA: Sem comentários explicativos no código

**Não crie comentários que apenas expliquem o que o código faz.**

1. Código deve ser autoexplicativo (nomes claros, métodos pequenos).
2. Comentários apenas quando forem parte de documentação gerada (ex.: anotações Swagger/OpenAPI no backend, JSDoc para tipos públicos quando gerar doc).
3. Não adicione comentários “explicativos” em controllers, services, componentes ou testes.

---

## Convenções do projeto

- **Backend:** Java 21 (Amazon Corretto), Spring Boot 3.x, Maven. API REST em `app/`; documentação via SpringDoc OpenAPI (Swagger). Banco: MariaDB 10.11 LTS; migrações com Liquibase. Credenciais e URLs via variáveis de ambiente.
- **Frontend:** React 18+, Vite, TypeScript, Tailwind, TanStack Query, React Router, React Hook Form, Zod. Código em `ui/src/` com estrutura por features (components, hooks, lib, pages, routes). Lint: ESLint com eslint-plugin-react, eslint-plugin-react-hooks e eslint-plugin-react-refresh (`cd ui && npm run lint`).
- **Containers:** desenvolvimento com Docker Compose (app + MariaDB); deploy local simulado com k3d (manifests em `k3d/`).

---

## Resumo de verificação antes de finalizar

- [ ] Testes executados (backend: `mvn verify`; front: `npm run test` se existir).
- [ ] Cobertura mínima de 80% respeitada (quando ferramenta de cobertura estiver em uso).
- [ ] Testes no padrão AAA; assertions claras.
- [ ] Containers recriados após mudanças: `docker compose down && docker compose up --build -d`.
- [ ] Lint/format do backend e do front executados e sem erros (front: `cd ui && npm run lint` com ESLint + plugins React).
- [ ] Se alterou Markdown: `npm run lint:docs` executado e sem erros.
- [ ] Documentação apenas no README (e em `k3d/README.md` quando for caso de k3d); ADRs em `adrs/`; histórico em CHANGELOG.md.
- [ ] Nenhum commit ou push realizado sem solicitação explícita.
