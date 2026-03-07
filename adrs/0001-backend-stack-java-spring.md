# ADR 0001 – Backend: Java 21 e Spring Boot

## Status

Aceito.

## Contexto

O projeto precisa de uma API REST estável, com ecossistema maduro, boa integração com banco relacional e documentação de API. A equipe tem familiaridade com Java e deseja usar uma stack moderna e LTS.

## Decisão

- **Linguagem:** Java 21 LTS (Amazon Corretto).
- **Framework:** Spring Boot 3.x (atualmente 3.4.4).
- **Build:** Maven; empacotamento em JAR executável.
- **Container:** Imagem base Amazon Corretto 21 (Alpine no estágio final do Dockerfile).

O backend fica inteiramente no diretório `app/`, com estrutura Maven padrão (`src/main/java`, `src/main/resources`). A API é exposta sob o prefixo `/api` (`server.servlet.context-path=/api`) para ser servida atrás do proxy reverso no mesmo host que a UI (ver ADR 0008).

## Consequências

- Uso de recursos modernos do Java 21 (records, virtual threads quando fizer sentido).
- Spring Boot fornece autoconfiguração, Actuator para health e integração com Liquibase e SpringDoc.
- Manutenção e atualizações seguem o ciclo LTS do Java e do Spring Boot.
