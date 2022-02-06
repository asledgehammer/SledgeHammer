package com.asledgehammer.sledgehammer.event

/**
 * **EventHandler** TODO: Document.
 *
 * @author Jab
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
annotation class EventHandler(

  /** TODO: Document. */

  val priority: Int = 0,

  /** TODO: Document. */
  val ignoreCancelled: Boolean = false,
)
