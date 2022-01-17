@file:Suppress("LeakingThis")

package com.asledgehammer.framework.task

import java.util.*

/**
 * TODO: Document.
 *
 * @author Jab
 *
 * @param id
 * @property delay
 * @property runnable
 */
open class DelayedTask(id: UUID, var delay: Long, val runnable: () -> Unit) {

    /**
     * TODO: Document.
     */
    var cancel = false

    /**
     * TODO: Document.
     */
    var ticksRemaining = delay
        internal set

    init {
        require(delay >= 0) { "delay cannot be negative." }
        TaskManager.register(id, this)
    }
}
