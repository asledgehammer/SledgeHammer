@file:Suppress("unused")

package com.asledgehammer.sledgehammer.command

/**
 * **CommandHandler** TODO: Document.
 *
 * @author Jab
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
annotation class CommandHandler(

  /** TODO: Document. */
  val commands: Array<String>,

  /** TODO: Document. */
  val permission: Array<String> = ["*"],

  /** TODO: Document. */
  val priority: Int = 0,

  /** TODO: Document. */
  val ignoreHandled: Boolean = false,
)
