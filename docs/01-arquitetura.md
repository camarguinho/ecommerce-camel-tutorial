# Arquitetura e Rotas

[Voltar ao Sumario](SUMMARY.md)

## Visao geral

O projeto foi estruturado para manter o transporte HTTP separado da logica interna. Essa decisao reduz acoplamento com Vert.x e permite testes unitarios baratos usando apenas `DefaultCamelContext`.

A organizacao atual do codigo acompanha essa separacao em pacotes:

- `com.example.ecommercecamel.route` concentra as `RouteBuilder`
- `com.example.ecommercecamel.config` concentra a infraestrutura local
- `com.example.ecommercecamel.support` concentra utilitarios de bootstrap e suporte operacional

## Rotas atuais

### `OrderHttpRoute`

Define apenas a exposicao do endpoint REST:

- `POST /api/orders` -> `direct:submit-order-rest`

### `OrderApiRoute`

Adapta o boundary HTTP para o fluxo interno:

- desserializa JSON para `CreateOrderRequest`
- chama `direct:submit-order`
- serializa a resposta para JSON
- converte erros de JSON invalido e requisicao invalida para payloads padronizados

### `OrderSubmissionRoute`

Executa a regra de negocio atual:

- valida `customerId`, itens, quantidade e preco
- cria um `OrderCreatedEvent`
- calcula `totalAmount`
- encaminha o evento para a rota de publicacao
- devolve `OrderAcceptedResponse`

### `OrderCreatedEventRoute`

Executa a integracao minima real do projeto:

- recebe o `OrderCreatedEvent`
- define a chave Kafka com `orderId`
- serializa o evento em JSON
- publica no endpoint configurado por `order.created.endpoint`

### `OrderIntegrationRoute`

Executa o fluxo de integracao de consumo:

- consome o topico Kafka `order-created`
- desserializa o payload para `OrderCreatedEvent`
- extrai headers para persistencia
- grava o registro em H2 in-memory via Camel SQL
- redireciona para DLT em caso de falha apos retries

### `HealthHttpRoute` e `HealthRoute`

Usam a mesma estrategia de separacao:

- `GET /health` -> `direct:health-check`
- `GET /h2-console` -> redirect para o servidor web do H2
- a resposta final eh JSON com `status=UP`

## Pacotes de suporte

### `InfrastructureConfiguration`

Fica em `config` e centraliza a criacao do `DataSource` H2 e a execucao das migrations Liquibase.

### `BootstrapProperties` e `H2ConsoleSupport`

Ficam em `support` para isolar leitura de propriedades e bootstrap do console H2 do resto das rotas.

## Fluxo atual do pedido

O fluxo possui dois caminhos que acontecem em momentos diferentes: a resposta HTTP eh devolvida logo apos a publicacao em Kafka, enquanto consumo e persistencia seguem de forma assincrona.

```text
HTTP POST /api/orders
        |
        v
direct:submit-order-rest
        |
        v
JSON -> CreateOrderRequest
        |
        v
direct:submit-order
        |
        v
OrderCreatedEvent
        |
        v
direct:publish-order-created
        |
        v
Kafka topic order-created
        |
        +--> HTTP 202-like business response (OrderAcceptedResponse)
        |
        v
consume-order-created-route
        |
        v
H2 webshop.ecommerce.order_events
```

## Proximo ponto de extensao natural

Agora que o `OrderCreatedEvent` ja eh publicado em Kafka, consumido e persistido em H2 in-memory, o proximo passo natural do tutorial eh introduzir um consumidor separado, comparar a mesma publicacao em RabbitMQ ou evoluir para uma persistencia relacional externa com Outbox Pattern.

[Anterior: Setup e Visao Geral](00-setup.md)

[Proximo: API REST e Fluxo do Pedido](02-api-rest-e-fluxo.md)