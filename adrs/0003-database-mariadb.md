# ADR 0003 – Banco de dados: MariaDB

## Status

Aceito.

## Contexto

A aplicação precisa de banco relacional para dados transacionais. É desejável usar uma versão LTS, com driver estável para Java e suporte a repositórios oficiais em Docker e Kubernetes.

## Decisão

- **SGBD:** MariaDB.
- **Versão:** 10.11 LTS (imagem `mariadb:10.11`).
- **Acesso:** Credenciais e nome do banco via variáveis de ambiente (MARIADB_*, SPRING_DATASOURCE_*); em Kubernetes, Secrets.
- **Persistência:** Em Docker Compose, volume nomeado; em k3d, PersistentVolumeClaim.

Nenhuma credencial é fixada em código ou em arquivos versionados.

## Consequências

- Compatibilidade com MySQL/MariaDB e ecossistema conhecido.
- Ciclo LTS previsível; imagens oficiais e healthcheck disponíveis para Compose e k8s.
