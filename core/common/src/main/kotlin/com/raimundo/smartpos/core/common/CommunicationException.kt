package com.raimundo.smartpos.core.common

/**
 * Qualquer falha na conversa com o backend: rede, HTTP != 2xx, payload que não decifra
 * ou resposta inconsistente. Para o fluxo de venda todas significam "desfazer".
 */
class CommunicationException(
    message: String,
    cause: Throwable? = null,
) : Exception(message, cause)
