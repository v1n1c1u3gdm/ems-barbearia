# ADR 0008 – Proxy reverso e ponto de entrada único

## Status

Aceito.

## Contexto

É desejável que o usuário acesse a aplicação por um único host e porta (ex.: localhost na porta 80 ou 443), com a UI na raiz, a API em um path prefixado e a documentação da API em um URL estável e fácil de lembrar.

## Decisão

- **Proxy reverso:** Nginx na raiz do Compose (`nginx/nginx.conf` montado no serviço `proxy`). O proxy é o único serviço que expõe porta(s) no host (80; 443 opcional, com certificados). Usa-se `resolver 127.0.0.11` e variáveis (`$backend`, `$frontend`) com `proxy_pass` para resolver `app` e `ui` em tempo de requisição, permitindo subir o proxy antes de app/ui.
- **UI na raiz:** `GET /` e demais paths que não sejam `/api` são encaminhados ao serviço `ui` (container que serve o build estático do frontend via Nginx). Suporte a SPA com `try_files` (fallback para `index.html`).
- **API em `/api`:** Backend Spring Boot com `server.servlet.context-path=/api`. O proxy encaminha `location /api/` para o container `app:8080`. Ex.: `GET /api/examples` → app; `GET /api/docs.html` → documentação interativa.
- **Documentação:** Acesso em `/api/docs.html`. Os paths `/api/docs` e `/api/doc.html` redirecionam para `/api/docs.html` (Nginx). Não se expõe `swagger-ui.html` como URL de entrada.
- **Porta no host:** O proxy expõe a porta 80 no host (`80:80`). Acesso em `http://localhost`. HTTPS opcional em 443 ao configurar certificados em `nginx/nginx.conf`.

## Consequências

- Localhost (ou o host configurado) responde com a UI em `/` e a API em `/api/`; mesma origem para o frontend, sem CORS para chamadas à API.
- Um único certificado e um único host/porta cobrem UI e API em cenários com HTTPS.
- Health do backend em `/api/actuator/health` (usado no k3d e em probes quando aplicável).
