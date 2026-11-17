# Guia de Saga de Pagamento

O Billing persiste o contexto das Sagas no DynamoDB. Localmente, as tabelas são
criadas pelo Compose central; em AWS, são provisionadas pelo repositório
`db-infra`.

## Fluxo

1. Ao gerar um PIX, `SagaPaymentService` cria o `SagaContext` e publica o evento
   de Saga iniciada.
2. Webhooks do Mercado Pago atualizam pagamento e Saga.
3. O scheduler procura Sagas `INICIADA` cuja expiração já passou, marca-as como
   expiradas e publica o evento correspondente.
4. O OS-Service consome os eventos para notificar o cliente e atualizar a ordem
   de serviço.

RabbitMQ é usado localmente (`MESSAGING_TYPE=rabbit`); na AWS, use SNS/SQS com
`MESSAGING_TYPE=aws`.

## Persistência DynamoDB

A tabela `billing-saga-contexts` usa `sagaId` como chave de partição e guarda o
objeto serializado no atributo `payload`. Os atributos de índice ficam no mesmo
item:

| Índice | Chave de partição | Chave de ordenação | Consulta |
|---|---|---|---|
| `pagamentoId-index` | `pagamentoId` | — | Sagas associadas a um pagamento |
| `ordemServicoId-status-index` | `ordemServicoId` | `status` | Verificar PIX pendente por OS |
| `status-dataExpiracao-index` | `status` | `dataExpiracao` | Encontrar Sagas iniciadas expiradas |

O DynamoDB usa a cadeia padrão de credenciais do AWS SDK. No `k8s-infra` atual,
os nodes EKS usam `LabRole`; essa role (ou uma role específica do workload, caso
IRSA/Pod Identity seja habilitado) precisa de `dynamodb:GetItem`,
`dynamodb:Query`, `dynamodb:PutItem` e `dynamodb:TransactWriteItems` nos recursos
pertinentes. O deploy não injeta credenciais AWS estáticas no Secret Kubernetes.

## Configuração local

No Compose central do projeto:

```bash
docker compose up -d dynamodb dynamodb-init
```

Inicie o Billing com `DYNAMODB_ENDPOINT=http://localhost:8000`. O endpoint deve
ficar vazio em AWS, para o SDK usar o serviço regional. RabbitMQ local pode ser
iniciado com `docker compose up -d rabbitmq`.

Para inspecionar Sagas no DynamoDB Local:

```bash
aws dynamodb query --endpoint-url http://localhost:8000 \
  --table-name billing-saga-contexts \
  --index-name ordemServicoId-status-index \
  --key-condition-expression 'ordemServicoId = :os' \
  --expression-attribute-values '{":os":{"S":"<ordem-servico-id>"}}' \
  --query 'Items[].payload.S'
```

## Verificações antes do deploy

- Provisionar tabelas e GSIs com `db-infra` antes de implantar o Billing.
- Configurar IAM para leitura, consulta, gravação e gravação transacional nas
  tabelas e índices do Billing.
- Configurar `AWS_REGION`, endpoints/ARNs SNS e filas SQS conforme o ambiente.
- Configurar as credenciais do Mercado Pago e validar a URL pública de webhook.
- Executar `mvn verify` e validar os fluxos de PIX, webhook, expiração e
  prevenção de PIX duplicado.
- Planejar a migração de dados MongoDB/DocumentDB separadamente: a troca do
  backend não copia registros existentes.
