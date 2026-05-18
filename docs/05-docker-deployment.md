# Docker e Ambiente Local

[Voltar ao Sumario](SUMMARY.md)

## Objetivo da stack

O `docker-compose.yml` foi criado com escopo preparado para os proximos passos do tutorial. O codigo atual ja usa Kafka para publicar e consumir `OrderCreatedEvent` e PostgreSQL para persistir esse evento. RabbitMQ permanece provisionado para os incrementos seguintes.

## Servicos do compose

### `app`

Container da aplicacao Camel empacotada a partir do jar sombreado.

Ele depende apenas dos servicos realmente usados no fluxo atual: Kafka e PostgreSQL.

### `postgres`

Banco usado pela persistencia do fluxo consumido e preparado para futuras evolucoes como pedido materializado e outbox.

O schema eh aplicado pela propria aplicacao via Liquibase no startup, portanto nao existe script SQL manual separado no compose para criar as tabelas principais.

### `zookeeper` e `kafka`

Stack local usada pela publicacao e pelo consumo atual do evento `order-created`.

### `kafdrop`

Interface web para visualizar topicos e mensagens.

### `rabbitmq`

Broker AMQP opcional com interface de management para futuras rotas RabbitMQ.

## Arquivos envolvidos

- `Dockerfile`
- `docker-compose.yml`
- `.env.example`
- `.dockerignore`

## Subindo o ambiente

```bash
docker compose up --build
```

## Enderecos uteis

- App HTTP: `http://localhost:8080`
- Health: `http://localhost:8080/health`
- Kafdrop: `http://localhost:9000`
- RabbitMQ Management: `http://localhost:15672`
- Kafka broker externo: `localhost:9092`
- PostgreSQL: `localhost:5432`

## Variaveis de ambiente

Copie os valores de `.env.example` para um `.env` local caso queira alterar portas, usuario ou senha.

## Importante

Hoje o app ja usa Kafka e PostgreSQL efetivamente. RabbitMQ continua no compose como apoio ao roadmap, mas nao faz parte do caminho de runtime atual.

[Anterior: Testes](04-testes.md)

[Proximo: Troubleshooting](06-troubleshooting.md)