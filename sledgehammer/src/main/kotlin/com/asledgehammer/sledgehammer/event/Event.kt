@file:Suppress("unused")

package com.asledgehammer.sledgehammer.event

/**
 * **Event** is the base class for all events to inherit for Sledgehammer.
 *
 * @author Jab
 */
abstract class Event {

  /**
   * The timestamp when the event is created.
   */
  val timestamp = System.currentTimeMillis()

  /**
   * If the event is handled by a listener.
   */
  var handled = false
}
