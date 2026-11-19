# ADR-0006: backend mockado como `Interceptor` do OkHttp, não um servidor

## Status
Aceito.

## Contexto
O desafio não fornece um backend real para rodar localmente, mas exige
testar os 23 cenários (aprovação, rejeição, timeout, falhas de rede em
pontos específicos, etc.) de forma reproduzível, sem depender de uma
ferramenta externa (ex.: WireMock rodando num container) que o avaliador
precisasse instalar.

## Decisão
`core:network-mock` (usado só pelo flavor `mock` de `:app`) implementa o
backend como um `okhttp3.Interceptor` (`MockBackendInterceptor`) registrado
no mesmo `OkHttpClient` que falaria com o backend real. Isso significa que:

- `HttpPaymentGateway`, a serialização JSON e a criptografia AES-256-GCM
  são **exatamente** os mesmos objetos que seriam usados contra o backend
  de produção — só o transporte HTTP é interceptado antes de sair do
  processo. Trocar para o backend real é literalmente não registrar esse
  interceptor (ver `app/src/mock` vs. `app/src/prod`).
- O cenário é escolhido pelos **centavos do valor digitado**
  (`AmountBasedScenarios`, ver tabela no README), o que deixa a demonstração
  manual (no app rodando) e os testes automatizados (`GatewayWithMockBackendTest`,
  `SmartPosE2ETest`) usando a mesma regra, sem um parâmetro de debug
  escondido.
- O mock mantém estado em memória (`HashMap<transactionId, Record>`) e
  replica regras do backend real que são essenciais para os testes:
  idempotência (mesmo id + mesmo payload = no-op; payload diferente = 409),
  e a "lápide" de um `/undo` que chega antes da `/authorization`.

## Consequências
- **A favor:** zero infraestrutura externa para rodar ou testar o projeto
  (`./gradlew testDebugUnitTest` ou instalar o APK `mock` já bastam); os
  testes de integração contra o mock (`GatewayWithMockBackendTest`) cobrem
  também a camada HTTP/criptografia reais, não só a lógica de negócio.
- **Contra:** o mock só existe enquanto o processo do app roda — ele não
  serve para testar o comportamento de **dois** terminais contra o mesmo
  backend (não é o caso aqui, cada POS fala com seu próprio terminal).
- O APK do flavor `prod` não inclui nenhuma classe de `core:network-mock`
  (verificado via `unzip -l` no `assembleProdRelease`, ver README > Qualidade).
