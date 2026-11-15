# Billing Service

Microsserviço Spring Boot preparado para seguir a estrutura em camadas do `os-service`, com build Maven, imagem Docker e deploy Helm no EKS.

## Estrutura

- `billing/src/main/java/com/techchallenge/billing`: aplicação organizada em `application`, `domain` e `infrastructure`.
- `billing/src/test/java/com/techchallenge/billing`: estrutura equivalente para os testes.
- O Billing usa DynamoDB; o Compose central sobe DynamoDB Local e cria as tabelas/índices locais.
- `.github/workflows/pipeline.yml`: build, testes, publicação no ECR e deploy no EKS pela branch `develop`.
- `k8s`: chart Helm do serviço.

## Configuração de infraestrutura

O chart publica o serviço `billing-service` no NodePort `30083` e no contexto `/v1/billing-service`. Configure a rota correspondente no API Gateway/NLB da infraestrutura antes de expor o serviço publicamente.

Em produção, o `db-infra` provisiona as tabelas DynamoDB. O Billing usa a cadeia padrão de credenciais do AWS SDK, sem credenciais AWS no Secret Kubernetes. No `k8s-infra` atual, os nodes EKS usam `LabRole`; essa role (ou uma role específica do workload, caso IRSA/Pod Identity seja habilitado) precisa permitir `dynamodb:GetItem`, `dynamodb:Query`, `dynamodb:PutItem` e `dynamodb:TransactWriteItems` nas tabelas e índices do Billing. A migração não copia automaticamente registros existentes do MongoDB/DocumentDB; faça a migração de dados separadamente antes de apontar tráfego de produção.

Para pagamentos em produção, configure os GitHub Actions Secrets `MERCADO_PAGO_ACCESS_TOKEN` e `MERCADO_PAGO_WEBHOOK_SECRET`. A pipeline consulta o endpoint do API Gateway `oficina-api-gateway` na região configurada e injeta somente essa URL base no ConfigMap. O `application.yaml` concatena a base, o contexto servlet (`/v1/billing-service`) e o caminho do controller (`/api/v1/billings/webhooks/mercadopago`); por isso, a rota do Billing na k8s-infra precisa estar provisionada antes do deploy. Localmente, a base padrão é `http://localhost:8082`. As credenciais AWS da pipeline precisam permitir `apigateway:GET` para consultar o Gateway. A pipeline injeta o token/segredo em um Kubernetes Secret. Não habilite `MERCADO_PAGO_SKIP_SIGNATURE_VALIDATION` em produção.

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
informe os nomes/ARNs dos tópicos SNS e das filas SQS do ambiente. O chart de
produção define `SPRING_CLOUD_AWS_SQS_QUEUE_NOT_FOUND_STRATEGY=FAIL` para impedir
que o Billing tente criar filas SQS ausentes. Localmente, o serviço usa RabbitMQ
(`MESSAGING_TYPE=rabbit`), sem criação de filas SQS.
Como a dependência AMQP continua no classpath para uso local, o chart de produção
define `RABBIT_HEALTH_CHECK_ENABLED=false` para não incluir o RabbitMQ inativo no
health geral quando o transporte usado é AWS.

Para persistência local, execute `docker compose up -d dynamodb dynamodb-init` no
Compose central e defina `DYNAMODB_ENDPOINT=http://localhost:8000` ao iniciar o
Billing fora do Compose. Em produção, deixe `DYNAMODB_ENDPOINT` vazio para usar
o endpoint regional da AWS e a role IAM do workload.
