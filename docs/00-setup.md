# Setup e Visao Geral

[Voltar ao Sumario](SUMMARY.md)

## Objetivo

Este projeto demonstra um fluxo inicial de e-commerce usando Apache Camel 4 em modo standalone com Java 17. O foco atual eh receber um pedido via HTTP, validar a carga, transformar o payload em um evento interno, publicar esse evento em Kafka, consumi-lo de volta, persisti-lo em H2 in-memory e devolver uma resposta de aceite na borda HTTP.

## Stack principal

- Java 17
- Maven 3.9+
- Apache Camel 4.6.0
- Camel Main
- Camel Platform HTTP Vert.x
- Camel Jackson
- H2 Database in-memory
- Liquibase
- JUnit 5 e AssertJ
- Logback

## O que ja existe

- Endpoint HTTP `POST /api/orders`
- Endpoint HTTP `GET /health`
- Rota interna `direct:submit-order`
- Publicacao do evento `OrderCreatedEvent` em Kafka
- Consumo do topico `order-created`
- Persistencia do evento em H2 in-memory
- Banco H2 `webshop` com schema `ecommerce` controlado por Liquibase
- Console web do H2 habilitado para inspecao local
- Dead-letter route para falhas de persistencia
- Validacao basica de payload
- Conversao de erro para respostas JSON coerentes
- Testes unitarios do fluxo interno, da adaptacao JSON, do health check, da publicacao do evento e do fluxo de consumo/persistencia
- Infra Docker preparada para Kafka e Kafdrop, com RabbitMQ opcional para evolucao futura e H2 embutido no app

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

Se quiser executar o tutorial com Kafka local e persistencia H2 embutida no app, suba a stack completa a partir da raiz do modulo:

```bash
docker compose up --build
```

Depois disso, os principais acessos ficam disponiveis em:

- App HTTP raiz: `http://localhost:8080` retorna um JSON simples com os endpoints disponiveis
- Health: `http://localhost:8080/health`
- H2 Console via app: `http://localhost:8080/h2-console`
- H2 Console direto: `http://localhost:8082`
- Kafdrop: `http://localhost:9000`

No H2 Console, use JDBC URL `jdbc:h2:mem:webshop;DB_CLOSE_DELAY=-1;MODE=PostgreSQL`, usuario `sa` e senha em branco. O schema de trabalho da aplicacao eh `ecommerce`.

Se precisar customizar portas, copie `.env.example` para `.env` antes de subir o compose.

### Executar localmente sem Docker

```bash
java -jar target/ecommerce-camel-tutorial-1.0.0-SNAPSHOT.jar
```

Nesse modo, o banco H2 ja nasce dentro da JVM. Para processar pedidos de ponta a ponta, voce ainda precisa de um Kafka acessivel em `localhost:9092` ou sobrescrever `kafka.bootstrap.servers`.

## Estrutura minima do codigo

```text
src/main/java/com/example/ecommercecamel/
  EcommerceCamelApplication.java
  config/
    InfrastructureConfiguration.java
  route/
    HealthHttpRoute.java
    HealthRoute.java
    OrderApiRoute.java
    OrderCreatedEventRoute.java
    OrderHttpRoute.java
    OrderIntegrationRoute.java
    OrderSubmissionRoute.java
    StartupRoute.java
  support/
    BootstrapProperties.java
    H2ConsoleSupport.java

tests/java/com/example/ecommercecamel/
  config/
    InfrastructureConfigurationTest.java
  route/
    HealthRouteTest.java
    OrderApiRouteTest.java
    OrderIntegrationRouteTest.java
    OrderSubmissionRouteTest.java
    StartupRouteTest.java
  support/
    H2ConsoleSupportTest.java
```

[Proximo: Arquitetura e Rotas](01-arquitetura.md)