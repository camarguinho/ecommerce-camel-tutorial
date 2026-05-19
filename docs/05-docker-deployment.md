# Docker e Ambiente Local

[Voltar ao Sumario](SUMMARY.md)

## Objetivo da stack

O `docker-compose.yml` foi criado com escopo preparado para os proximos passos do tutorial. O codigo atual usa Kafka para publicar e consumir `OrderCreatedEvent`, enquanto a persistencia local roda em H2 in-memory dentro do proprio processo da aplicacao. RabbitMQ permanece provisionado para os incrementos seguintes.

## Servicos do compose

### `app`

Container da aplicacao Camel empacotada a partir do jar sombreado.

Ele depende apenas do Kafka para o fluxo atual. O banco H2 e o console web sobem dentro do mesmo processo Java.

### `h2`

Nao existe container separado para o banco. O app cria o `DataSource` H2 in-memory no bootstrap, aplica Liquibase automaticamente e expoe o console web para inspecao local.

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
- H2 Console: `http://localhost:8082`
- Kafdrop: `http://localhost:9000`
- RabbitMQ Management: `http://localhost:15672`
- Kafka broker externo: `localhost:9092`

No H2 Console, conecte usando JDBC `jdbc:h2:mem:ecommerce;DB_CLOSE_DELAY=-1;MODE=PostgreSQL`, usuario `sa` e senha vazia.

## Variaveis de ambiente

Copie os valores de `.env.example` para um `.env` local caso queira alterar as portas expostas.

## Importante

Hoje o app ja usa Kafka e H2 efetivamente. RabbitMQ continua no compose como apoio ao roadmap, mas nao faz parte do caminho de runtime atual.

[Anterior: Testes](04-testes.md)

[Proximo: Troubleshooting](06-troubleshooting.md)