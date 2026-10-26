# Billing Service

Microsserviço Spring Boot preparado para seguir a estrutura em camadas do `os-service`, com build Maven, imagem Docker e deploy Helm no EKS.

## Estrutura

- `billing/src/main/java/com/techchallenge/billing`: aplicação organizada em `application`, `domain` e `infrastructure`.
- `billing/src/test/java/com/techchallenge/billing`: estrutura equivalente para os testes.
- MongoDB é o banco de dados do serviço; as coleções e índices locais são inicializados pelo Compose central do projeto.
- `.github/workflows/pipeline.yml`: build, testes, publicação no ECR e deploy no EKS pela branch `develop`.
- `k8s`: chart Helm do serviço.

## Configuração de infraestrutura

O chart publica o serviço `billing-service` no NodePort `30083` e no contexto `/v1/billing-service`. Configure a rota correspondente no API Gateway/NLB da infraestrutura antes de expor o serviço publicamente.

Em produção, o deploy conecta ao MongoDB Atlas usando o segredo `MONGODB_ATLAS_URI` do GitHub Actions. Cadastre nele a URI `mongodb+srv://` do cluster Atlas, com a senha URL-encoded; a pipeline injeta o valor como `MONGODB_URI` no Secret Kubernetes do serviço. Configure também a Network Access do Atlas para permitir a conexão de saída do EKS (ou configure VPC peering/private endpoint).

## Build local

```powershell
cd billing
.\mvnw.cmd -B verify
```

## Mensageria local

O perfil local usa RabbitMQ (`MESSAGING_TYPE=rabbit`). Suba o broker pelo Compose
central do projeto (`docker compose up -d rabbitmq`) e execute os serviços OS e
Billing com as configurações padrão de host/porta e credenciais `local/local`.
O painel de gerenciamento fica disponível em `http://localhost:15672`.

No EKS, os charts Helm selecionam `MESSAGING_TYPE=aws`; nessa configuração,
informe os nomes/ARNs dos tópicos SNS e das filas SQS do ambiente.
