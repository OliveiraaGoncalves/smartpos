# ADR-0003: AES-256-GCM sobre HTTPS (não CBC, não HMAC separado)

## Status
Aceito.

## Contexto
O desafio exige HTTPS **e** uma camada adicional de AES-256 no corpo da
requisição (defesa em profundidade: mesmo um TLS mal configurado ou um
proxy MITM corporativo não expõe o valor/forma de pagamento em claro).

## Decisão
Usar **AES-256-GCM** (`AesGcmPayloadCipher`, `core:security`) como a camada
adicional, com:

- **IV de 96 bits aleatório** (`SecureRandom`) a cada mensagem, nunca
  reutilizado — reuso de IV com a mesma chave quebra a confidencialidade do
  GCM.
- **AAD (`Aad.of(metodo, path, transactionId)`)** amarrando o ciphertext ao
  endpoint e à transação: um corpo capturado em `/authorization` não decifra
  se reenviado para `/undo`, nem se associado a outro `transactionId`
  (`AesGcmPayloadCipherTest > corpo de authorization nao pode ser
  reaproveitado no undo`).
- **Resposta do polling também cifrada e autenticada** — sem isso, alguém
  que decifrasse o TLS poderia forjar um `APROVADA`.
- `kid` (key id) no envelope para permitir rotação de chave sem versionar o
  protocolo.

GCM foi escolhido sobre AES-256-**CBC** porque é cifragem **autenticada**:
além do sigilo, garante que o corpo não foi alterado (um `amount`
adulterado no meio do caminho é rejeitado na decriptação, não é preciso um
HMAC/assinatura separada por cima do CBC).

## Consequências
- **A favor:** um único primitivo (GCM) cobre sigilo + integridade +
  autenticidade do payload; menos superfície de erro do que
  implementar CBC + HMAC manualmente (ex.: esquecer de usar HMAC
  constant-time compare, um clássico de vulnerabilidade de padding oracle).
- **Contra:** GCM é mais sensível a reuso de IV do que CBC com IV
  reaproveitado "só" compromete aquela mensagem — por isso o IV é gerado
  por mensagem com `SecureRandom`, nunca derivado de contador persistido
  entre processos.
- **Chave:** no flavor `mock` há uma chave de **desenvolvimento** fixa no
  `BuildConfig`, documentada como tal e nunca usada no flavor `prod`. Em
  produção (`AndroidKeystoreKeyProvider`) a chave é gerada e vive só no
  Android Keystore (`setBlockModes(GCM)`, não exportável) — ver ADR-0007.
