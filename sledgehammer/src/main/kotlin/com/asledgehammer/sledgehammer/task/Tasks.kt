@file:Suppress("unused")

package com.asledgehammer.sledgehammer.task

import java.util.*

/**
 * **Tasks** TODO: Document.
 *
 * @author Jab
 */
object Tasks {

  private val tasks = HashMap<UUID, ArrayList<DelayedTask>>()

  /** TODO: Document. */
  @JvmStatic
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
            if (task is Task) task.delay = task.period
            else iterator.remove()
          } else {
            task.delay--
          }
        }
      }
    }
  }

  /** TODO: Document. */
  @JvmStatic
  fun register(id: UUID, delayedTask: DelayedTask) {
    tasks.computeIfAbsent(id) { ArrayList() }.add(delayedTask)
  }

  /** TODO: Document. */
  @JvmStatic
  fun unregister(id: UUID) {
    tasks.remove(id)
  }
}
