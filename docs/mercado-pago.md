# Mercado Pago — integração do Billing

O Billing cria cobranças Pix usando a **Orders API** (`POST
https://api.mercadopago.com/v1/orders`). Não usa o endpoint legado `/v1/payments`.
O token de acesso é usado somente pelo backend. A URL informada em
`MERCADO_PAGO_BASE_URL` compõe a configuração local da notificação, mas o código
atual não envia uma `notification_url` no corpo da criação da order. Configure
o webhook no painel do Mercado Pago, além de configurar a aplicação.

## Aplicação e credenciais de teste

1. Acesse [Mercado Pago Developers](https://www.mercadopago.com.br/developers/pt)
   com sua conta e abra [Suas integrações](https://www.mercadopago.com.br/developers/panel/app).
2. Crie uma aplicação para **Checkout Transparente** usando a **Orders API**.
   No painel, selecione o ambiente **Teste** e copie o Access Token de teste.
   O Billing não precisa da Public Key, pois a integração com a Orders API é
   feita pelo backend.
3. Se a demonstração exigir um usuário comprador, crie uma **conta de teste**
   do tipo Comprador na seção de contas de teste da aplicação. Use o usuário e
   a senha gerados para entrar no Mercado Pago em uma janela anônima ou perfil
   separado; consulte no painel o código de verificação, se solicitado.

O ambiente de teste e as credenciais de teste não são uma conta real de
pagamento. A conta de teste do Mercado Pago também não substitui o cliente
cadastrado no `catalog-service`. Não publique tokens, senhas ou segredos em
commits, logs ou capturas de tela.

## Webhook e assinatura

O controller recebe `POST /api/v1/billings/webhooks/mercadopago`. No EKS, o
chart acrescenta o contexto `/v1/billing-service`, então a URL pública fica:

```text
https://<api-gateway>/v1/billing-service/api/v1/billings/webhooks/mercadopago
```

No painel da aplicação, em **Teste → Configuração de Webhooks**, configure a
URL HTTPS e habilite notificações de **Orders**. Copie a assinatura secreta
gerada para `MERCADO_PAGO_WEBHOOK_SECRET`. O controller valida `type=order`,
ações `order.updated`, `order.processed` ou `order.canceled`, o ID da order,
`data.id`, os headers `x-signature` e `x-request-id`, e a assinatura tem
tolerância de cinco minutos.

### Testar localmente com ngrok

1. Inicie o Billing na porta padrão `8082` e exponha-a em um terminal:

   ```powershell
   ngrok http 8082
   ```

2. Configure no painel do Mercado Pago a URL HTTPS exibida pelo ngrok,
   seguida de `/api/v1/billings/webhooks/mercadopago`. Sem
   `SERVER_SERVLET_CONTEXT_PATH`, esse é o caminho local completo.
3. Antes de iniciar o Billing, configure as variáveis no mesmo terminal
   PowerShell:

   ```powershell
   $env:MERCADO_PAGO_ACCESS_TOKEN = '<access-token-de-teste>'
   $env:MERCADO_PAGO_WEBHOOK_SECRET = '<assinatura-secreta>'
   $env:MERCADO_PAGO_BASE_URL = 'https://<host-ngrok>'
   $env:MERCADO_PAGO_SKIP_SIGNATURE_VALIDATION = 'false'
   ```

   `MERCADO_PAGO_BASE_URL` deve conter somente a origem, sem caminho nem barra
   final. Mantenha o túnel ativo e atualize a URL cadastrada se o endereço
   mudar. O ngrok expõe seu serviço local à internet: use-o somente em testes
   e encerre-o ao terminar.

### Deploy no EKS

No repositório, configure em **Settings → Secrets and variables → Actions**:

| Secret | Uso |
|---|---|
| `MERCADO_PAGO_ACCESS_TOKEN` | Token de acesso da aplicação e ambiente escolhidos |
| `MERCADO_PAGO_WEBHOOK_SECRET` | Segredo de assinatura do webhook |

O workflow de `develop` consulta o endpoint do API Gateway `oficina-api-gateway`
na região `us-east-1`, exige uma URL pública HTTPS e injeta a origem no ConfigMap
e as credenciais no Secret Kubernetes. A rota do Billing em `k8s-infra` deve
existir antes do deploy. A role da pipeline precisa de `apigateway:GET`.

> **Importante:** o `k8s/values-prod.yaml` atual define
> `MERCADO_PAGO_SKIP_SIGNATURE_VALIDATION: "true"` e o workflow não altera esse
> valor. Com o bypass ativado, configurar `MERCADO_PAGO_WEBHOOK_SECRET` não
> valida a assinatura recebida. Antes de disponibilizar o webhook, altere o
> valor para `"false"` e confirme o ConfigMap aplicado. Não use credenciais de
> teste para pagamentos reais.

## Fluxo e verificação

1. O OS publica o evento de orçamento; o Billing consulta a OS, cria uma order
   Pix e persiste orçamento, pagamento e contexto da Saga no DynamoDB.
2. O Mercado Pago envia um webhook de order. O Billing valida a assinatura,
   registra o evento e consulta a order atual em `GET /v1/orders/{orderId}` com
   o Access Token configurado. O processamento posterior é assíncrono.
3. O Billing atualiza o pagamento e publica o evento de resultado; o OS
   consome o evento e atualiza o estado da ordem. Sagas pendentes são
   verificadas periodicamente, e webhooks com falha podem ser tentados
   novamente.

O ID da order do Mercado Pago não é o UUID da OS nem o ID interno do pagamento.
Em testes de demonstração, foi observado que consultar novamente a order após
selecionar a simulação de aprovação ajudou a refletir seu status no sandbox:

```bash
curl --location 'https://api.mercadopago.com/v1/orders/<NUMERO_ORDEM>' \
  --header 'Content-Type: application/json' \
  --header 'Authorization: Bearer <TOKEN_DE_TESTE>'
```

Use o Access Token de teste da mesma aplicação. Esse GET consulta o estado da
order; não é uma operação de aprovação garantida, nem deve ser interpretado
como fluxo de produção. Ele também não substitui a entrega do webhook ao
Billing. Após a notificação, confira os logs e:

- `GET /api/v1/billings/os/{osId}/pagamentos` para o status do pagamento;
- `GET /api/v1/billings/orcamentos/{orcamentoId}` para o status do orçamento;
- `GET /v1/os/{osId}` para confirmar a transição da OS.

## Referências

- [Criar aplicação e obter credenciais](https://www.mercadopago.com.br/developers/pt/docs/checkout-api-orders/create-application)
- [Contas de teste](https://www.mercadopago.com.br/developers/pt/docs/checkout-api-orders/resources/test-accounts)
- [Notificações da Orders API](https://www.mercadopago.com.br/developers/pt/docs/checkout-api-orders/notifications)
- [Documentação do ngrok](https://ngrok.com/docs/start)
