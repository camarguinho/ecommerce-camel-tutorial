# Setup e Visao Geral

[Voltar ao Sumario](SUMMARY.md)

## Objetivo

Este projeto demonstra um fluxo inicial de e-commerce usando Apache Camel 4 em modo standalone com Java 17. O foco atual eh receber um pedido via HTTP, validar a carga, transformar o payload em um evento interno, publicar esse evento em Kafka, consumi-lo de volta, persisti-lo em PostgreSQL e devolver uma resposta de aceite na borda HTTP.

## Stack principal

- Java 17
- Maven 3.9+
- Apache Camel 4.6.0
- Camel Main
- Camel Platform HTTP Vert.x
- Camel Jackson
- Liquibase
- JUnit 5 e AssertJ
- Logback

## O que ja existe

- Endpoint HTTP `POST /api/orders`
- Endpoint HTTP `GET /health`
- Rota interna `direct:submit-order`
- Publicacao do evento `OrderCreatedEvent` em Kafka
- Consumo do topico `order-created`
- Persistencia do evento em PostgreSQL
- Schema de banco controlado por Liquibase
- Dead-letter route para falhas de persistencia
- Validacao basica de payload
- Conversao de erro para respostas JSON coerentes
- Testes unitarios do fluxo interno, da adaptacao JSON, do health check, da publicacao do evento e do fluxo de consumo/persistencia
- Infra Docker preparada para PostgreSQL, Kafka e Kafdrop, com RabbitMQ opcional para evolucao futura

## O que ainda nao existe

- Publicacao efetiva em RabbitMQ
- Dependencias de runtime para RabbitMQ ainda nao foram adicionadas ao build atual
- Metricas exportadas para Prometheus ou equivalente

## Comandos principais

### Rodar testes

```bash
mvn test
```

### Gerar o jar executavel

```bash
mvn clean package
```

### Subir o ambiente com Docker Compose

Se quiser executar o tutorial com Kafka e PostgreSQL locais, suba a stack completa a partir da raiz do modulo:

```bash
docker compose up --build
```

Depois disso, os principais acessos ficam disponiveis em:

- App HTTP raiz: `http://localhost:8080` retorna um JSON simples com os endpoints disponiveis
- Health: `http://localhost:8080/health`
- Kafdrop: `http://localhost:9000`
- PostgreSQL: `localhost:5432`

Se precisar customizar portas ou credenciais, copie `.env.example` para `.env` antes de subir o compose.

### Executar localmente sem Docker

```bash
java -jar target/ecommerce-camel-tutorial-1.0.0-SNAPSHOT.jar
```

## Estrutura minima do codigo

```text
src/main/java/com/example/ecommercecamel/
  EcommerceCamelApplication.java
  OrderHttpRoute.java
  OrderApiRoute.java
  OrderSubmissionRoute.java
  OrderCreatedEventRoute.java
  OrderIntegrationRoute.java
  HealthHttpRoute.java
  HealthRoute.java
```

[Proximo: Arquitetura e Rotas](01-arquitetura.md)