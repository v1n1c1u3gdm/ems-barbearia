# K3d – Deploy local

Simula um deploy em Kubernetes usando [k3d](https://k3d.io/).

## Pré-requisitos

- Docker
- [k3d](https://k3d.io/v5.x/docs/installation/)
- [kubectl](https://kubernetes.io/docs/tasks/tools/)

## Uso rápido

1. Na raiz do repositório, crie e ajuste o `.env`:

   ```bash
   cp .env.example .env
   ```

2. Crie os secrets e o cluster (o script cria o cluster, builda a imagem do app, importa no k3d, cria os secrets e aplica os manifests):

   ```bash
   ./k3d/cluster.sh
   ```

3. Obtenha a URL do serviço da API:

   ```bash
   kubectl get svc app -n ems-barbearia
   ```

   Use o `EXTERNAL-IP` (ou localhost com a porta mapeada, ex.: 8080) para acessar a API e o Swagger.

## Passo a passo manual

1. Criar o cluster:

   ```bash
   k3d cluster create ems-barbearia --port 8080:30880 --agents 1
   ```

2. Buildar e importar a imagem do app:

   ```bash
   docker build -t ems-barbearia/app:latest ./app
   k3d image import ems-barbearia/app:latest -c ems-barbearia
   ```

3. Criar os secrets a partir do `.env`:

   ```bash
   ./k3d/create-secret.sh
   ```

4. Aplicar os manifests (namespace já é criado pelo create-secret.sh). Em `mariadb-deployment.yaml` o PVC vem antes do Deployment para o scheduler encontrar o volume já criado:

   ```bash
   kubectl apply -f k3d/namespace.yaml
   kubectl apply -f k3d/mariadb-deployment.yaml
   kubectl apply -f k3d/app-deployment.yaml
   ```

5. Acompanhar o rollout:

   ```bash
   kubectl rollout status deployment/mariadb -n ems-barbearia
   kubectl rollout status deployment/app -n ems-barbearia
   ```

## Acessar a API e o Swagger

- API: `http://<EXTERNAL-IP>:8080` (ou a porta que o LoadBalancer expuser).
- Documentação da API: `http://<EXTERNAL-IP>:8080/api/docs.html`

## Limpar

```bash
k3d cluster delete ems-barbearia
```

## Problemas comuns

- **Pods em Pending / "disk-pressure":** os nós do k3d podem receber o taint `node.kubernetes.io/disk-pressure` quando o disco do host está cheio. Libere espaço no disco (ex.: `docker system prune`, remover clusters k3d não usados) ou rode o k3d em um ambiente com espaço livre.
- **PVC Pending:** o manifest do MariaDB foi ordenado com o PVC primeiro; se o PVC continuar Pending, verifique o provisioner (local-path) com `kubectl get pods -n kube-system` e os eventos com `kubectl describe pvc mariadb-pvc -n ems-barbearia`.
