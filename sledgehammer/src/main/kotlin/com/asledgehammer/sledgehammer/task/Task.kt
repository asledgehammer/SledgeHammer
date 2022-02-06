package com.asledgehammer.sledgehammer.task

import java.util.*

/**
 * **Task** TODO: Document.
 *
 * @author Jab
 *
 * @param id
 * @param delay
 * @property period
 * @param runnable
 */
class Task(id: UUID, delay: Long, var period: Long, runnable: () -> Unit) : DelayedTask(id, delay, runnable) {
  init {
    require(delay > -1) { "delay cannot be negative." }
    require(period > -1) { "period cannot be negative." }
  }
}
