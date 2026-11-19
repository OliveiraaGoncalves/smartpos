# ADR-0007: Hilt para injeção de dependência

## Status
Aceito.

## Contexto
Com ~20 módulos e 8 features, cada um com seu próprio `ViewModel` e casos
de uso, um grafo de DI manual (`AppContainer`, como no protótipo de
módulo único que este projeto substituiu) cresceria para dezenas de
`provide*()` espalhados e seria fácil esquecer de atualizar ao adicionar
uma dependência nova.

## Decisão
Usar Hilt em todos os módulos Android (`smartpos.android.hilt` convention
plugin): `@HiltViewModel` nos ViewModels, `@HiltWorker` no
`RetryPendingUndoWorker`, e módulos `@InstallIn(SingletonComponent::class)`
por camada (`core:data/di/DataModule.kt`) e por flavor
(`app/src/mock/.../MockNetworkModule.kt`, `app/src/prod/.../ProdNetworkModule.kt`).

`core:model`/`core:common`/`core:domain` continuam módulos Kotlin puros
(`jvm`), mas suas classes com `@Inject constructor` (casos de uso,
`TransactionEngine`, `SystemClock`, `UuidIdGenerator`) funcionam tanto
injetadas pelo Hilt em produção quanto instanciadas manualmente nos testes
de unidade — nenhum desses módulos depende do Hilt para compilar.

## Consequências
- **A favor:** adicionar uma dependência nova é só mudar o construtor
  (`@Inject constructor(novoParam: X)`); o Dagger/Hilt detecta em tempo de
  compilação qualquer binding faltando — foi assim que apareceram e foram
  corrigidos, durante a montagem do `:app` na Fase 8, os construtores sem
  `@Inject` de `TransactionEngine`/`SimulatedReceiptPrinter`/`SystemClock`/
  `UuidIdGenerator` e o `@Provides` faltante de `WorkManager`/`EngineConfig`.
- **Contra:** geração de código via KSP adiciona tempo de build (`kspDebugKotlin`
  roda em praticamente todo módulo Android) e os erros de grafo do Dagger só
  aparecem ao compilar o módulo que efetivamente monta o componente
  (`:app`), não nos módulos que só declaram o `@Inject constructor` — por
  isso o Dagger não pegou esses buracos durante as Fases 2-7, só na Fase 8.
- Dagger **não** honra valores default de parâmetro em `@Inject constructor`
  (gera dois construtores sintéticos e falha com "may only contain one
  injected constructor"): toda classe `@Inject`-injetada neste projeto tem
  todos os parâmetros explícitos, com o valor antes "default" promovido a
  um `@Provides` (`EngineConfig`) ou a uma constante interna da classe
  (`SimulatedReceiptPrinter.PRINT_DURATION_MS`).
