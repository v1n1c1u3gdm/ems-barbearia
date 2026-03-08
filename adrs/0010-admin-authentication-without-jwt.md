# ADR 0010 – Autenticação da área administrativa (sem JWT nas APIs)

## Status

Aceito.

## Contexto

A área administrativa (`/admin`) precisa de controle de acesso: apenas usuários autorizados devem acessar o painel e as telas de Contatos, Promoções, Agendamentos e Clientes. Inicialmente não é requisito proteger as APIs com token JWT; a proteção é apenas na camada de apresentação.

## Decisão

- **Backend:** Tabela `admin_user` (username, password_hash em BCrypt), populada via Liquibase. Endpoint `POST /auth/login` recebe usuário e senha, valida contra o banco e retorna um token simbólico (string fixa `"authenticated"`). Não há emissão de JWT nem validação de token nas rotas da API; `SecurityConfig` mantém `anyRequest().permitAll()`.
- **Usuários padrão:** Inseridos por migrations (ex.: `admin`/`password`, `adm-alessandra`/`Emerson@123`). Credenciais documentadas no README; devem ser alteradas em produção.
- **Frontend:** Tela de login em `/admin/login` chama a API de login e, em sucesso, grava o token em `sessionStorage` (`auth_token`). O componente `ProtectedAdminRoute` verifica presença do token; se ausente, redireciona para `/admin/login`. Logout limpa o `sessionStorage` e redireciona para a tela de login.
- **Rotas protegidas:** Todas as rotas filhas de `/admin` exceto `/admin/login` ficam sob `ProtectedAdminRoute` (Painel, Contatos, Promoções, Agendamentos, Clientes).

## Consequências

- Acesso à área admin exige login; usuário não autenticado é redirecionado para `/admin/login`.
- As APIs em `/api/admin/*` continuam acessíveis sem header de autorização; proteção real da API pode ser introduzida em ADR futuro (ex.: JWT ou sessão servidor).
- Uso de `sessionStorage` implica que a “sessão” não persiste ao fechar a aba e não é compartilhada entre abas.
- Novos usuários admin podem ser adicionados via novas migrations Liquibase (insert em `admin_user` com hash BCrypt).
