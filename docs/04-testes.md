# Testes

[Voltar ao Sumario](SUMMARY.md)

## Estrategia atual

O projeto prioriza testes baratos e focados usando `DefaultCamelContext`, sem subir infraestrutura HTTP real quando isso nao eh necessario.

## Suites atuais

### `StartupRouteTest`

Garante que a rota minima de bootstrap continua funcional.

### `OrderSubmissionRouteTest`

Valida:

- criacao do evento interno
- calculo do total
- publicacao do evento para endpoint substituivel por `mock:`
- rejeicao de requisicoes sem `customerId`
- rejeicao de requisicoes sem itens

### `OrderApiRouteTest`

Valida:

- JSON valido no boundary de adaptacao
- resposta de erro para payload invalido
- resposta de erro para JSON malformado

### `HealthRouteTest`

Valida o retorno do health check interno.

### `OrderIntegrationRouteTest`

Valida:

- consumo do evento por endpoint configuravel
- envio para o endpoint de persistencia
- envio para DLT quando a persistencia falha

### `InfrastructureConfigurationTest`

Valida:

- bootstrap do `DataSource` H2
- execucao das migrations Liquibase
- criacao das estruturas `order_events`, `order_event_dlt` e `order_event_summary`

## Comando

```bash
mvn test
```

## Proximos testes recomendados

- teste HTTP real com o app executando em porta local
- teste de publicacao para RabbitMQ quando a rota AMQP for introduzida
- teste de integracao com Kafka real via Docker Compose
- teste de execucao do app com H2 Console acessivel

[Anterior: Configuracao e Observabilidade](03-configuracao-observabilidade.md)

[Proximo: Docker e Ambiente Local](05-docker-deployment.md)