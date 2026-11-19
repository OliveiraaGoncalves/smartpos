# ADR-0001: Modularização por camada e por feature

## Status
Aceito.

## Contexto
O desafio pede Clean Architecture + MVVM + SOLID "com modularização por
camada e por feature". Um único módulo `:app` cresceria rápido e misturaria
regra de negócio, I/O e UI, dificultando reaproveitar `core:domain` em um
possível segundo app (ex.: um app de configuração de terminal) e tornando
builds incrementais lentas (qualquer alteração recompila tudo).

## Decisão
Dividir em módulos Gradle por responsabilidade:

- **`core:model`, `core:common`, `core:domain`** — Kotlin puro (`jvm`), sem
  Android. Carregam as regras de negócio e não podem depender de
  `core:data`/`core:network`/`core:local-storage` nem de nenhum `feature:*`.
- **`core:security`, `core:network`, `core:network-mock`, `core:local-storage`,
  `core:printer`, `core:data`** — implementações Android das interfaces de
  `core:domain` (Room, OkHttp, Keystore, impressora).
- **`core:ui`** — design system (tema, componentes), sem regra de negócio.
- **`core:testing`** — fakes e fixtures compartilhados, só para `testImplementation`.
- **`feature:*`** — uma tela (ou grupo pequeno de telas) por módulo. Um
  `feature:*` nunca depende de outro `feature:*` nem de `core:data`/`core:network`/
  `core:local-storage` diretamente: só de `core:domain` (via casos de uso) e
  `core:ui`. Isso é imposto manualmente (revisão), não por um lint de
  dependências — ver "Próximos passos" no README.
- **`:app`** — o único módulo que conhece todos os `feature:*` e monta o grafo
  Hilt (incluindo os módulos específicos de flavor).

As regras comuns a cada tipo de módulo (Android Gradle Plugin, Compose,
Hilt, Room, Detekt/Spotless/Kover) ficam em convention plugins em
`build-logic/convention`, para não repetir configuração em 20 `build.gradle.kts`.

## Consequências
- **A favor:** `core:domain` compila e testa em segundos (puro JVM, sem
  emulador/Robolectric); um `feature:*` muda sem recompilar os outros;
  times diferentes poderiam trabalhar em features diferentes com conflito
  mínimo de merge.
- **Contra:** mais boilerplate de módulo (cada `feature:*` tem seu próprio
  `build.gradle.kts`, `AndroidManifest.xml` mínimo, pacote de DI) e a
  primeira configuração (build-logic) tem um custo inicial maior do que um
  único módulo.
- Como não há um plugin tipo Now-in-Android's `nowinandroid-checkdependencies`
  aqui, a regra "feature não depende de feature" é verificada por revisão de
  PR, não pelo Gradle. Risco aceito e documentado.
