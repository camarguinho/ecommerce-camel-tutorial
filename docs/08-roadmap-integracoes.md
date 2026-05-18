# Roadmap de Integracoes

[Voltar ao Sumario](SUMMARY.md)

## Estado atual

O projeto ja publica `OrderCreatedEvent` em Kafka com `orderId` como chave, consome esse mesmo evento e o persiste em PostgreSQL com tratamento de falha por DLT.

## Proximo incremento recomendado

Separar o consumidor em outro contexto ou modulo, ou introduzir uma segunda integracao real com RabbitMQ.

## Caminho 1: Kafka

Extrair o consumidor Kafka para outro modulo do tutorial, permitindo mostrar integracao entre dois contextos independentes.

## Caminho 2: RabbitMQ

Adicionar `camel-spring-rabbitmq` ao build e publicar o mesmo evento em exchange dedicada, com roteamento por tipo de evento.

## Caminho 3: PostgreSQL

Persistir o pedido recebido antes da publicacao de evento. O projeto hoje persiste o evento consumido; o passo seguinte mais rico eh persistir o comando inicial e evoluir para Outbox Pattern.

## Caminho 4: Observabilidade

Adicionar `camel-micrometer` e `camel-management` e conecta-los a um backend real de metricas e dashboards.


## Ordem sugerida

1. Consumo assicrono por outro componente
2. Persistencia do pedido na entrada
3. Comparacao com publicacao em RabbitMQ
4. Metricas e tracing

[Anterior: Boas Praticas com Camel](07-boas-praticas-camel.md)