package com.raimundo.smartpos.core.model

/** Preferência de tema do operador, persistida entre sessões. */
enum class ThemeMode(
    val label: String,
) {
    SYSTEM("Automático (sistema)"),
    LIGHT("Claro"),
    DARK("Escuro"),
}
