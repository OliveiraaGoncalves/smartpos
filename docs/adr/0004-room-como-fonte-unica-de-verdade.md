# ADR-0004: Room (write-ahead) como fonte única de verdade da transação

## Status
Aceito.

## Contexto
Nenhuma etapa do fluxo (autorizar, aguardar resultado, desfazer) pode ser
perdida se o app for matado pelo sistema no meio do caminho — o cenário 15
("recuperação após reinício") exige que uma transação órfã seja desfeita
quando o app reabre.

## Decisão
`TransactionCoordinatorRepository` (`core:data`) usa o `Flow` do Room
(`TransactionDao`) como **única fonte de verdade** do estado da UI — não há
estado duplicado em memória que possa dessincronizar do banco. Toda
transição de `TransactionEngine` grava no `TransactionJournal`
(`RoomTransactionJournal`) **antes** de seguir para a próxima chamada de
rede ("write-ahead": grava a intenção antes de executá-la, não depois).

No `Application.onCreate()`, `TransactionCoordinatorRepository.start()`
consulta `TransactionDao.getNonFinal()` (estados não finais) e
`getPendingUndo()` (`DESFAZIMENTO_PENDENTE`): qualquer transação órfã é
retomada — se estava em `AGUARDANDO_RESULTADO`, o /undo é disparado
assumindo que o app morreu no meio do polling; se já estava
`DESFAZIMENTO_PENDENTE`, o retry continua.

## Consequências
- **A favor:** não existe um "estado da UI" e um "estado real" podendo
  divergir — a tela de Transações observa exatamente o que está no banco.
  A recuperação após `kill -9` do processo é testada de ponta a ponta em
  `TransactionCoordinatorRepositoryTest > recuperacaoAoAbrirDesfazTransacaoOrfa`,
  rodando em um emulador real (Gradle Managed Device), não um mock de disco.
- **Contra:** cada transição de estado é uma escrita no SQLite antes da
  chamada de rede, o que adiciona uma latência pequena e constante (não
  perceptível no POS, mas é uma troca deliberada de performance por
  durabilidade).
- O esquema do Room é versionado e o JSON exportado
  (`core/local-storage/schemas/.../SmartPosDatabase/1.json`) fica commitado,
  para detectar migrações destrutivas acidentais no CI.
