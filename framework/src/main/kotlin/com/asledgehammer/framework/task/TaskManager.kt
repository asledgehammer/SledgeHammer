@file:Suppress("unused")

package com.asledgehammer.framework.task

import java.util.*

/**
 * TODO: Document.
 *
 * @author Jab
 */
object TaskManager {

    private val tasks = HashMap<UUID, ArrayList<DelayedTask>>()

    /**
     * TODO: Document.
     */
    fun tick() {
        if (tasks.isNotEmpty()) {
            for ((_, tasks) in tasks) {
                if (tasks.isEmpty()) continue
                val iterator = tasks.iterator()
                while (iterator.hasNext()) {
                    val task = iterator.next()
                    if (task.cancel) {
                        iterator.remove()
                        continue
                    }
                    if (task.delay <= 0L) {
                        task.runnable()
                        if (task is TimerTask) task.delay = task.period
                        else iterator.remove()
                    } else {
                        task.delay--
                    }
                }
            }
        }
    }

    /**
     * TODO: Document.
     */
    fun register(id: UUID, delayedTask: DelayedTask) {
        tasks.computeIfAbsent(id) { ArrayList() }.add(delayedTask)
    }

    /**
     * TODO: Document.
     */
    fun unregister(id: UUID) {
        tasks.remove(id)
    }
}
