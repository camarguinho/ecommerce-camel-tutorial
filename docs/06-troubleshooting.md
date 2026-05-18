# Troubleshooting

[Voltar ao Sumario](SUMMARY.md)

## `NoSuchBeanException: platform-http-router`

Esse erro costuma aparecer em teste unitario quando uma rota com `platform-http` eh carregada sem o runtime Vert.x. A estrategia correta eh manter a definicao HTTP separada da rota interna testavel.

## `INVALID_JSON`

O payload enviado para `POST /api/orders` nao eh um JSON valido. Verifique aspas, virgulas e estrutura do corpo da requisicao.

## `INVALID_REQUEST`

O JSON foi parseado, mas o conteudo falhou na validacao. Casos comuns:

- `customerId` vazio
- lista de itens vazia
- `quantity <= 0`
- `unitPrice < 0`

## Porta 8080 ocupada

Altere o valor de `HTTP_PORT` no `.env` ou no `docker compose`.

## Compose sobe, mas a app falha no health check

Verifique:

- se o container `app` concluiu o build
- se a rota `GET /health` responde localmente
- se o `JAVA_OPTS` no compose nao sobrescreveu a porta errada

[Anterior: Docker e Ambiente Local](05-docker-deployment.md)

[Proximo: Boas Praticas com Camel](07-boas-praticas-camel.md)