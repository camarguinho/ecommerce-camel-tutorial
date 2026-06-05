# Boas Praticas com Camel

[Voltar ao Sumario](SUMMARY.md)

## 1. Separe transporte de logica interna

O projeto adota essa pratica ao separar:

- `OrderHttpRoute` da adaptacao `OrderApiRoute`
- `HealthHttpRoute` da resposta `HealthRoute`

Essa separacao agora tambem fica explicita na estrutura fisica do projeto, com as rotas concentradas em `com.example.ecommercecamel.route` e os utilitarios de infraestrutura fora do pacote raiz.

Isso melhora testabilidade e reduz dependencia de infraestrutura no teste.

## 2. Prefira rotas pequenas e nomeadas

Cada rota atual possui responsabilidade curta e `routeId` explicito. Isso melhora rastreabilidade em log e futuras metricas.

## 3. Converta erros na borda

Erros de JSON invalido e de validacao devem ser tratados no boundary HTTP, nao vazados como stack trace bruto para o cliente.

## 4. Externalize configuracao

Portas e endpoints de infraestrutura nao devem ficar presos ao codigo. O projeto usa propriedades e `JAVA_OPTS` para manter essa flexibilidade.

## 5. Preserve rotas internas testaveis

Rotas `direct:` continuam sendo o ponto mais barato para validar fluxo, transformacao e contratos antes de subir o ambiente completo.

## 6. Nao prometa infraestrutura sem explicitar estado de uso

Kafka e RabbitMQ seguem provisionados no ambiente local, mas nem toda a infraestrutura tem o mesmo nivel de uso. Hoje o fluxo ativo usa Kafka na publicacao e no consumo e H2 in-memory na persistencia; RabbitMQ permanece provisionado como extensao futura e a documentacao precisa deixar isso explicito.

## 7. Tire DDL do caminho manual da aplicacao

O projeto usa Liquibase para controlar o schema do H2 in-memory. Isso reduz drift entre ambientes e evita depender de criacao manual de tabela para que as rotas Camel funcionem.

## 8. Nao carregue dependencia futura no build atual

Servico opcional em `docker-compose.yml` eh aceitavel para apoiar os proximos capitulos. Dependencia Java e propriedade de runtime sem rota ativa nao sao. A base atual foi ajustada para manter no build apenas Kafka, H2 e Liquibase, deixando RabbitMQ e metricas como passos explicitos de evolucao.

[Anterior: Troubleshooting](06-troubleshooting.md)

[Proximo: Roadmap de Integracoes](08-roadmap-integracoes.md)