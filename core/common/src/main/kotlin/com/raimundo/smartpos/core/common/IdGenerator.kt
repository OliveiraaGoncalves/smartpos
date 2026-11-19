package com.raimundo.smartpos.core.common

import java.util.UUID

/** Gera o transactionId. Injetável para que os testes de toque duplo controlem a unicidade. */
interface IdGenerator {
    fun newId(): String
}

class UuidIdGenerator : IdGenerator {
    override fun newId(): String = UUID.randomUUID().toString()
}
