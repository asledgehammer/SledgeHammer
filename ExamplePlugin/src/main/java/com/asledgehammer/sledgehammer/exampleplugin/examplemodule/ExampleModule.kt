@file:Suppress("unused")

package com.asledgehammer.sledgehammer.exampleplugin.examplemodule

import com.asledgehammer.crafthammer.api.event.EventHandler
import com.asledgehammer.crafthammer.api.event.EventListener
import com.asledgehammer.crafthammer.api.event.log.LogEntry
import com.asledgehammer.crafthammer.api.event.log.LogListener
import com.asledgehammer.crafthammer.api.event.network.PreLoginEvent
import com.asledgehammer.crafthammer.api.event.player.PlayerJoinEvent
import com.asledgehammer.sledgehammer.plugin.CraftModule

/**
 * **ExampleModule** TODO: Document.
 *
 * @author Jab
 */
class ExampleModule : CraftModule(), EventListener, LogListener {

  override fun onEnable() {
    addEventListener(this)
    // addLogListener(this)
  }

  // Example LogListener method.
  override fun onLogEntry(entry: LogEntry) {}

  // Example event handlers.
  @EventHandler(priority = 1, ignoreCancelled = true)
  fun on(event: PreLoginEvent) {
    if (event.connection.username.equals("Jab", true)) {
      event.reject("Nope.")
    }
  }

  @EventHandler
  fun on(event: PlayerJoinEvent) {
    log("Player ${event.player.username} has joined!")
  }
}