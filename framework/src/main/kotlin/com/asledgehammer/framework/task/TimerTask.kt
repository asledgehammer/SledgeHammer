package com.asledgehammer.framework.task

import java.util.*

/**
 * TODO: Document.
 *
 * @author Jab
 *
 * @param id
 * @param delay
 * @property period
 * @param runnable
 */
class TimerTask(id: UUID, delay: Long, var period: Long, runnable: () -> Unit) : DelayedTask(id, delay, runnable) {
    init {
        require(period >= 0) { "period cannot be negative." }
    }
}
