# Tour Navegavel do Projeto

[Voltar ao Sumario](SUMMARY.md)

## Como usar este tour

Este guia foi escrito para voce aprender Apache Camel olhando o codigo na mesma ordem em que o fluxo real acontece. A ideia eh alternar entre:

1. o capitulo conceitual em `docs/`
2. a classe `RouteBuilder` correspondente
3. o teste que prova aquele comportamento

Se voce seguir essa sequencia, vai entender rapido a divisao entre borda HTTP, fluxo interno `direct:`, integracao externa e infraestrutura de suporte.

## 1. Comece pela visao geral

Leia primeiro:

- [00-setup.md](00-setup.md)
- [01-arquitetura.md](01-arquitetura.md)

Depois abra estes arquivos do projeto:

- [pom.xml](../pom.xml)
- [src/main/resources/application.properties](../src/main/resources/application.properties)
- [src/main/java/com/example/ecommercecamel/route/StartupRoute.java](../src/main/java/com/example/ecommercecamel/route/StartupRoute.java)

O que aprender aqui:

- Camel esta sendo usado em modo standalone, sem Spring Boot
- o projeto privilegia rotas pequenas, com `routeId` explicito
- propriedades controlam endpoints reais, retries e portas
- a unidade central de composicao eh a rota, nao um controller tradicional

Pergunta util enquanto voce le:

`Se o transporte mudar, quais rotas continuam iguais?`

A resposta ja aparece na arquitetura: as rotas `direct:` concentram a regra que independe de HTTP.

## 2. Veja a borda HTTP mais fina possivel

Leia o capitulo:

- [02-api-rest-e-fluxo.md](02-api-rest-e-fluxo.md)

Depois navegue por estas classes, nesta ordem:

1. [src/main/java/com/example/ecommercecamel/route/OrderHttpRoute.java](../src/main/java/com/example/ecommercecamel/route/OrderHttpRoute.java)
2. [src/main/java/com/example/ecommercecamel/route/OrderApiRoute.java](../src/main/java/com/example/ecommercecamel/route/OrderApiRoute.java)
3. [src/main/java/com/example/ecommercecamel/route/OrderSubmissionRoute.java](../src/main/java/com/example/ecommercecamel/route/OrderSubmissionRoute.java)

Como ler esse trecho:

- `OrderHttpRoute` so expoe `POST /api/orders` e aponta para `direct:submit-order-rest`
- `OrderApiRoute` faz o trabalho de adaptacao: JSON entra, objeto sai, erro vira resposta padronizada
- `OrderSubmissionRoute` executa a regra de negocio local: valida, cria evento, calcula total e monta a resposta

Conceitos de Camel que aparecem aqui:

- `from(...)` define o consumidor da rota
- `to(...)` encadeia o proximo endpoint
- `process(...)` permite codigo Java quando a DSL sozinha nao basta
- `exchangeProperty(...)` guarda estado intermediario sem poluir o body
- `marshal()` e `unmarshal()` fazem transformacoes de payload

Teste correspondente:

- [tests/java/com/example/ecommercecamel/route/OrderApiRouteTest.java](../tests/java/com/example/ecommercecamel/route/OrderApiRouteTest.java)
- [tests/java/com/example/ecommercecamel/route/OrderSubmissionRouteTest.java](../tests/java/com/example/ecommercecamel/route/OrderSubmissionRouteTest.java)

Aprendizado principal:

Camel nao exige que voce misture transporte e regra. Aqui, HTTP so entrega o payload ao fluxo interno. Isso deixa a regra reaproveitavel e muito mais facil de testar.

## 3. Siga o evento para fora da aplicacao

Depois da submissao, abra:

1. [src/main/java/com/example/ecommercecamel/route/OrderCreatedEventRoute.java](../src/main/java/com/example/ecommercecamel/route/OrderCreatedEventRoute.java)
2. [src/main/resources/application.properties](../src/main/resources/application.properties)

O que observar:

- a rota publica a partir de `direct:publish-order-created`
- o header `kafka.KEY` recebe `orderId`
- o destino real nao esta hardcoded na classe; vem de `order.created.endpoint`

Esse eh um padrao importante em Camel: a rota define a intencao, enquanto o endpoint concreto pode variar por propriedade.

Teste correspondente:

- [tests/java/com/example/ecommercecamel/route/OrderSubmissionRouteTest.java](../tests/java/com/example/ecommercecamel/route/OrderSubmissionRouteTest.java)

Pergunta util:

`O que muda se eu trocar Kafka por outro broker?`

No desenho atual, muda principalmente o endpoint configurado e a rota de integracao associada. A regra que cria o evento continua igual.

## 4. Entenda o fluxo assincrono de consumo e persistencia

Agora leia estas pecas:

1. [src/main/java/com/example/ecommercecamel/route/OrderIntegrationRoute.java](../src/main/java/com/example/ecommercecamel/route/OrderIntegrationRoute.java)
2. [03-configuracao-observabilidade.md](03-configuracao-observabilidade.md)
3. [04-testes.md](04-testes.md)

Essa rota ensina varios fundamentos de integracao em Camel:

- consumo a partir de endpoint externo configuravel
- captura do payload bruto antes do parse
- transformacao JSON para `OrderCreatedEvent`
- enriquecimento com headers para persistencia
- retry com `onException(...)`
- desvio para DLT quando a persistencia falha

Abra junto o teste:

- [tests/java/com/example/ecommercecamel/route/OrderIntegrationRouteTest.java](../tests/java/com/example/ecommercecamel/route/OrderIntegrationRouteTest.java)

Esse teste eh especialmente didatico porque mostra duas ideias fortes de Camel:

- voce consegue substituir Kafka e SQL por endpoints `direct:` e `mock:`
- a semantica da rota continua a mesma, mesmo sem infraestrutura real

Se quiser aprender Camel de forma pragmatica, esse eh um dos melhores arquivos do projeto para estudar com calma.

## 5. Veja como a infraestrutura e mantida fora das rotas

Leia agora:

1. [src/main/java/com/example/ecommercecamel/config/InfrastructureConfiguration.java](../src/main/java/com/example/ecommercecamel/config/InfrastructureConfiguration.java)
2. [src/main/java/com/example/ecommercecamel/support/BootstrapProperties.java](../src/main/java/com/example/ecommercecamel/support/BootstrapProperties.java)
3. [src/main/java/com/example/ecommercecamel/support/H2ConsoleSupport.java](../src/main/java/com/example/ecommercecamel/support/H2ConsoleSupport.java)
4. [src/main/resources/db/changelog/db.changelog-master.xml](../src/main/resources/db/changelog/db.changelog-master.xml)
5. [src/main/resources/db/changelog/changes/001-create-order-tables.sql](../src/main/resources/db/changelog/changes/001-create-order-tables.sql)

O que aprender aqui:

- Camel integra com infraestrutura registrada no contexto, como `DataSource`
- Liquibase prepara o schema antes de a integracao com SQL ser usada
- propriedades de bootstrap sao resolvidas antes do runtime completo do Camel
- o console H2 eh um apoio operacional, nao parte da regra de negocio

Essa separacao e importante porque evita rotas inchadas com detalhes de infraestrutura.

## 6. Observe o segundo exemplo de separacao: health e discovery

Abra nesta ordem:

1. [src/main/java/com/example/ecommercecamel/route/HealthHttpRoute.java](../src/main/java/com/example/ecommercecamel/route/HealthHttpRoute.java)
2. [src/main/java/com/example/ecommercecamel/route/HealthRoute.java](../src/main/java/com/example/ecommercecamel/route/HealthRoute.java)

O mesmo padrao reaparece:

- a rota HTTP exposta e pequena
- a resposta real vem de uma rota `direct:`
- o redirecionamento do H2 fica encapsulado numa rota propria

Isso reforca a principal licao arquitetural da literatura: em Camel, transporte e orquestracao devem ficar desacoplados sempre que possivel.

## 7. Leia as boas praticas depois de ver o codigo

Agora faz sentido voltar para:

- [07-boas-praticas-camel.md](07-boas-praticas-camel.md)
- [08-roadmap-integracoes.md](08-roadmap-integracoes.md)

Esses dois capitulos ficam mais valiosos depois que voce ja viu as rotas reais, porque deixam claro o motivo de algumas decisoes do projeto:

- manter `direct:` como ponto interno testavel
- externalizar endpoint e retry em propriedades
- nao adicionar dependencia futura antes de existir rota ativa
- tratar erro na borda, nao no cliente

## 8. Ordem de estudo recomendada

Se voce quiser estudar Camel com foco em aprendizado incremental, siga esta ordem:

1. [01-arquitetura.md](01-arquitetura.md)
2. [src/main/java/com/example/ecommercecamel/route/OrderHttpRoute.java](../src/main/java/com/example/ecommercecamel/route/OrderHttpRoute.java)
3. [src/main/java/com/example/ecommercecamel/route/OrderApiRoute.java](../src/main/java/com/example/ecommercecamel/route/OrderApiRoute.java)
4. [src/main/java/com/example/ecommercecamel/route/OrderSubmissionRoute.java](../src/main/java/com/example/ecommercecamel/route/OrderSubmissionRoute.java)
5. [src/main/java/com/example/ecommercecamel/route/OrderCreatedEventRoute.java](../src/main/java/com/example/ecommercecamel/route/OrderCreatedEventRoute.java)
6. [src/main/java/com/example/ecommercecamel/route/OrderIntegrationRoute.java](../src/main/java/com/example/ecommercecamel/route/OrderIntegrationRoute.java)
7. [tests/java/com/example/ecommercecamel/route/OrderSubmissionRouteTest.java](../tests/java/com/example/ecommercecamel/route/OrderSubmissionRouteTest.java)
8. [tests/java/com/example/ecommercecamel/route/OrderIntegrationRouteTest.java](../tests/java/com/example/ecommercecamel/route/OrderIntegrationRouteTest.java)
9. [src/main/java/com/example/ecommercecamel/config/InfrastructureConfiguration.java](../src/main/java/com/example/ecommercecamel/config/InfrastructureConfiguration.java)
10. [07-boas-praticas-camel.md](07-boas-praticas-camel.md)

## 9. Exercicios praticos para fixar

1. Troque `order.created.endpoint` para `mock:order-created` em um teste novo e observe como a regra continua igual.
2. Adicione um novo header em `OrderIntegrationRoute` antes da persistencia e cubra isso com teste.
3. Crie uma rota nova `direct:validate-order` e extraia parte da validacao para estudar composicao de rotas.
4. Simule outro tipo de erro na borda HTTP e veja em que lugar ele deve ser tratado: `OrderApiRoute` ou rota interna.

## 10. Nota sobre o bootstrap

O [pom.xml](../pom.xml) referencia a classe `com.example.ecommercecamel.EcommerceCamelApplication` no `maven-shade-plugin`, mas esse arquivo nao aparece em `src/main/java` no estado atual do workspace. Para o estudo das rotas isso nao bloqueia nada, mas vale revisar se a classe esta fora do recorte atual ou se falta sincronizar o fonte com o build.

[Anterior: Roadmap de Integracoes](08-roadmap-integracoes.md)