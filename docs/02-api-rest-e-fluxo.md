# API REST e Fluxo do Pedido

[Voltar ao Sumario](SUMMARY.md)

## Endpoint disponivel

### Criar pedido

```http
POST /api/orders
Content-Type: application/json
```

#### Exemplo de requisicao

```json
{
  "customerId": "de305d54-75b4-431b-adb2-eb6b9e546014",
  "simulatePaymentFailure": false,
  "items": [
    {
      "productId": "8f95de2b-5c39-4b72-9c6a-6f793f4dc001",
      "quantity": 1,
      "unitPrice": 350.00
    }
  ]
}
```

#### Exemplo de resposta com sucesso

```json
{
  "orderId": "2f47d5c8-3fef-4af8-9ec4-80c3d9f2f4e4",
  "status": "RECEIVED",
  "totalAmount": 350.00
}
```

## Erros mapeados

### JSON invalido

```json
{
  "code": "INVALID_JSON",
  "message": "Request body must contain valid JSON"
}
```

### Requisicao invalida

```json
{
  "code": "INVALID_REQUEST",
  "message": "customerId is required"
}
```

## Endpoint de health

```http
GET /health
```

### Resposta esperada

```json
{
  "status": "UP",
  "service": "ecommerce-camel-tutorial"
}
```

## O que acontece depois do aceite

Quando a requisicao eh valida:

1. o payload vira `CreateOrderRequest`
2. a regra de negocio gera um `OrderCreatedEvent`
3. o evento eh publicado no topico Kafka `order-created`
4. a API devolve `OrderAcceptedResponse`

Esse recorte implementa o nivel minimo recomendado de integracao real para o tutorial: entrada HTTP, transformacao, saida externa e tratamento basico de erro.

## Exemplo com curl

```bash
curl -X POST 'http://localhost:8080/api/orders' \
  -H 'Content-Type: application/json' \
  -d '{
    "customerId": "de305d54-75b4-431b-adb2-eb6b9e546014",
    "simulatePaymentFailure": false,
    "items": [
      {
        "productId": "8f95de2b-5c39-4b72-9c6a-6f793f4dc001",
        "quantity": 1,
        "unitPrice": 350.00
      }
    ]
  }'
```

[Anterior: Arquitetura e Rotas](01-arquitetura.md)

[Proximo: Configuracao e Observabilidade](03-configuracao-observabilidade.md)