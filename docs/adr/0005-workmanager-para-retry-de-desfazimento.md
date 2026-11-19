# ADR-0005: WorkManager para retentar desfazimentos pendentes

## Status
Aceito.

## Contexto
Se o `/undo` falhar todas as tentativas imediatas (`TransactionEngine`
tenta 3 vezes com backoff), a transação fica `DESFAZIMENTO_PENDENTE` — ela
**não** pode ser dada como desfeita sem confirmação do backend, mas também
não se pode ficar tentando para sempre dentro do processo do app (ele pode
ser fechado).

## Decisão
`PendingUndoWorkScheduler` agenda um `RetryPendingUndoWorker`
(`@HiltWorker`, periodicidade de 15 min — o mínimo permitido pelo
`PeriodicWorkRequest` do Android) que roda mesmo com o app em background ou
fechado, e tenta de novo o `/undo` de toda transação em
`DESFAZIMENTO_PENDENTE`.

WorkManager foi escolhido (em vez de, por exemplo, um `AlarmManager` +
`BroadcastReceiver`, ou simplesmente deixar para a próxima abertura do
app) porque:
- sobrevive a reinícios do processo e do próprio aparelho (persiste no
  Room interno do WorkManager);
- já integra com Hilt (`HiltWorkerFactory` + `Configuration.Provider` em
  `SmartPosApplication`) sem precisar de um `Service` próprio;
- `ExistingPeriodicWorkPolicy.KEEP` garante que reabrir o app não agenda um
  segundo worker duplicado.

## Consequências
- **A favor:** o terminal eventualmente converge para `DESFEITA` mesmo que
  o operador nunca mais abra o app enquanto a rede estiver instável — sem
  esse mecanismo, uma transação `DESFAZIMENTO_PENDENTE` só seria retentada
  na próxima vez que alguém abrisse a tela de Pendências manualmente.
- **Contra:** 15 minutos é o intervalo mínimo garantido pelo sistema para
  trabalho periódico — não dá para ir mais rápido sem usar um
  `OneTimeWorkRequest` encadeado manualmente, o que adicionaria
  complexidade sem necessidade real aqui (um `/undo` pendente não bloqueia
  novas vendas, só aparece no banner "não entregue a mercadoria").
- A tela de Pendências (`feature:pendings`) também oferece "tentar desfazer
  agora", para o operador não precisar esperar o próximo ciclo do Worker.
