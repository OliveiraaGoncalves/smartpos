package com.raimundo.smartpos.core.common

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class IoDispatcher

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class DefaultDispatcher

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class MainDispatcher

/** Escopo de aplicação (SupervisorJob): vendas sobrevivem à navegação e à rotação de tela. */
@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class ApplicationScope
