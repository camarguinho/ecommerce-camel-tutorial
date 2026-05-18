# Configuracao e Observabilidade

[Voltar ao Sumario](SUMMARY.md)

## Arquivos de runtime

### `src/main/resources/application.properties`

Centraliza defaults locais para:

- porta HTTP
- conexao com Kafka
- endpoint de publicacao `order.created.endpoint`
- conexao com PostgreSQL
- politicas de retry da integracao Kafka -> PostgreSQL

## Liquibase

O schema do banco nao eh mais pressuposto pelo codigo. Ele eh criado no bootstrap da aplicacao via Liquibase, usando o mesmo `DataSource` registrado para as rotas Camel.

Os changelogs ficam em `src/main/resources/db/changelog` e hoje cobrem:

- DDL para `order_events`
- DDL para `order_event_dlt`
- uma view `order_event_summary` criada a partir de `SELECT`, usada como camada inicial de leitura do fluxo persistido

Essa abordagem elimina a dependencia de criacao manual das tabelas antes de subir a aplicacao.

### `src/main/resources/logback.xml`

Define logging de console com foco em:

- logs do Apache Camel em `INFO`
- logs da aplicacao em `INFO`
- root logger em `WARN`

## Override por ambiente

No ambiente containerizado, o projeto usa `JAVA_OPTS` com propriedades JVM, por exemplo:

```bash
-Dhttp.port=8080
-Dkafka.bootstrap.servers=kafka:9092
-Dpostgres.host=postgres
```

Essa abordagem permite manter defaults locais simples e ainda adaptar o runtime para Docker sem mudar o codigo.

RabbitMQ continua apenas como servico opcional do compose e como tema do roadmap. Como nao existe rota AMQP ativa, o runtime atual nao carrega propriedades nem dependencia Java dessa integracao.

## Observabilidade minima implementada

- logs de startup do Camel
- log de aceite de pedido com `orderId` e total
- log do fluxo de submissao antes da publicacao
- endpoint `GET /health`

## Observabilidade futura

Metricas detalhadas e gerenciamento avancado continuam no roadmap. Quando essa fase chegar, o passo correto sera adicionar `camel-micrometer` e `camel-management` junto com o backend real de metricas, em vez de manter dependencias ociosas no build atual.

[Anterior: API REST e Fluxo do Pedido](02-api-rest-e-fluxo.md)

[Proximo: Testes](04-testes.md)