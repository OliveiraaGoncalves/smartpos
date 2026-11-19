# ADR-0002: Jetpack Compose em vez de Views/XML

## Status
Aceito.

## Contexto
O desafio pede para justificar a escolha de UI. As telas do POS (Home,
Venda, Detalhe, Comprovante, Transações, Pendências) são, no fundo, funções
de um estado que muda sozinho em background: uma venda vai de `CRIADA` a
`APROVADA`/`REJEITADA`/`DESFEITA` sem nenhuma ação do operador, e a tela
precisa refletir isso.

## Decisão
Usar Jetpack Compose (Material 3) em vez de Views + XML + ViewBinding.

- A UI é `@Composable fun Tela(state: TelaUiState, ...)`: dado o mesmo
  `state`, a tela sempre renderiza igual. Isso elimina uma classe inteira de
  bug de View (esquecer de atualizar um campo, Adapter dessincronizado da
  lista).
- `StateFlow<UiState>` do ViewModel vira `collectAsStateWithLifecycle()`: a
  tela de Transações redesenha sozinha quando o Room emite uma mudança,
  sem `notifyDataSetChanged`/`DiffUtil` manual.
- Testabilidade: `createComposeRule`/`createAndroidComposeRule` com
  semântica (`onNodeWithText`, `contentDescription`) substitui Espresso +
  `ViewMatchers`; os testes em `core:ui` (`NumericKeypadTest`,
  `PosButtonTest`, `StatusChipTest`) e o E2E do `:app` usam exatamente essa
  API.
- Menos código: não existe par Activity/Fragment + layout XML + Adapter
  para cada tela.

## Consequências
- **A favor:** menos boilerplate, testes de UI mais diretos, Preview com
  `@Preview` para iterar sem instalar no device (`SaleScreenPreview`,
  `HomeScreenPreview` etc., em claro e escuro).
- **Contra:** o APK fica um pouco maior que a alternativa em Views (runtime
  do Compose) e a curva de aprendizado de recomposição/side-effects é real.
  Em um POS com pouco armazenamento isso pesaria; não é o caso aqui.
- Decisão de design: nenhum botão fica "morto". `PosButton` sempre dispara
  `onClick`, mesmo com `enabled = false` visualmente — quem chama decide
  mostrar um snackbar explicando o motivo (ver `core:ui/PosButton.kt`).
