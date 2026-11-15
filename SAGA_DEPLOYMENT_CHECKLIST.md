# Checklist de Deploy do Billing e das Sagas

## Pré-requisitos

- [ ] Java 21 e Maven disponíveis.
- [ ] RabbitMQ local ou SNS/SQS na AWS configurados.
- [ ] Tabelas e índices DynamoDB provisionados antes do deploy do Billing.
- [ ] Role runtime do EKS (`LabRole` nos nodes do `k8s-infra` atual, ou role
  específica caso IRSA/Pod Identity esteja habilitado) com `dynamodb:GetItem`,
  `dynamodb:Query`, `dynamodb:PutItem` e `dynamodb:TransactWriteItems`.
- [ ] Credenciais e URL de webhook do Mercado Pago configuradas.

## Desenvolvimento local

No Compose central:

```bash
docker compose up -d dynamodb dynamodb-init rabbitmq
```

Inicie o Billing com `DYNAMODB_ENDPOINT=http://localhost:8000` e
`MESSAGING_TYPE=rabbit`. Execute os testes e o build:

```bash
cd billing
mvn -B verify
```

## AWS / EKS

- [ ] Executar `db-infra` e confirmar que as tabelas DynamoDB e GSIs estão
  `ACTIVE`.
- [ ] Confirmar que os pods usam uma identidade IAM com permissões DynamoDB; no
  `k8s-infra` atual, o acesso padrão vem da role dos nodes (`LabRole`).
- [ ] Manter `DYNAMODB_ENDPOINT` vazio para usar o endpoint AWS regional.
- [ ] Configurar `AWS_REGION`, tópicos SNS e filas SQS.
- [ ] Confirmar que a role pode consultar os índices e executar gravações
  transacionais nas tabelas do Billing.
- [ ] Fazer deploy do Billing e validar readiness, criação de orçamento,
  atualização via webhook e expiração de Saga.

## Verificação local de uma Saga

```bash
aws dynamodb query --endpoint-url http://localhost:8000 \
  --table-name billing-saga-contexts \
  --index-name ordemServicoId-status-index \
  --key-condition-expression 'ordemServicoId = :os' \
  --expression-attribute-values '{":os":{"S":"<ordem-servico-id>"}}' \
  --query 'Items[].payload.S'
```

## Dados existentes e rollback

- [ ] Planejar e validar a migração dos registros MongoDB/DocumentDB para
  DynamoDB. O deploy não migra dados automaticamente.
- [ ] Fazer backup/exportação antes da migração e validar contagens e amostras.
- [ ] Definir um plano de rollback compatível com as gravações realizadas após
  o corte; não alternar o serviço para um banco antigo sem reconciliar dados.
- [ ] Monitorar erros de autorização DynamoDB, falhas de webhook e Sagas
  pendentes/expiradas após o deploy.
