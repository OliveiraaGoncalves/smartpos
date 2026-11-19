package com.raimundo.smartpos.core.testing

import com.raimundo.smartpos.core.common.Clock

/** Delega para o relógio virtual do `runTest` (`testScheduler.currentTime`) ou qualquer outra fonte. */
class TestClock(
    private val now: () -> Long,
) : Clock {
    override fun nowMillis(): Long = now()
}
