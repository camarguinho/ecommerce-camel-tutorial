# Testes

[Voltar ao Sumario](SUMMARY.md)

## Estrategia atual

O projeto prioriza testes baratos e focados usando `DefaultCamelContext`, sem subir infraestrutura HTTP real quando isso nao eh necessario.

Os testes acompanham a organizacao do codigo em `route`, `config` e `support`, o que reduz imports artificiais e preserva o encapsulamento quando algum helper usa visibilidade de pacote.

## Suites atuais

### Pacote `route`

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

### Pacote `config`

### `InfrastructureConfigurationTest`

Valida:

- bootstrap do `DataSource` H2
- execucao das migrations Liquibase
- criacao do schema `ecommerce`
- criacao das estruturas `ecommerce.order_events`, `ecommerce.order_event_dlt` e `ecommerce.order_event_summary`

### Pacote `support`

### `H2ConsoleSupportTest`

Valida:

- geracao do arquivo temporario `.h2.server.properties`
- configuracao do console H2 com a conexao embutida da aplicacao

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