@file:Suppress("unused")

package com.asledgehammer.sledgehammer.command

/**
 * **Commander** TODO: Document.
 *
 * @author Jab
 */
interface Commander {

  /** TODO: Document. */
  fun sendMessage(vararg messages: String)

  /** TODO: Document. */
  fun hasPermission(permission: String): Boolean
}
