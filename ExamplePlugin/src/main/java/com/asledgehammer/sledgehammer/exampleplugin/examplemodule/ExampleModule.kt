@file:Suppress("unused")

package com.asledgehammer.sledgehammer.exampleplugin.examplemodule

import com.asledgehammer.crafthammer.api.command.CommandExecution
import com.asledgehammer.crafthammer.api.command.CommandHandler
import com.asledgehammer.crafthammer.api.entity.Player
import com.asledgehammer.crafthammer.api.event.EventHandler
import com.asledgehammer.crafthammer.api.event.EventListener
import com.asledgehammer.crafthammer.api.event.log.LogEntry
import com.asledgehammer.crafthammer.api.event.log.LogListener
import com.asledgehammer.crafthammer.api.event.network.PreLoginEvent
import com.asledgehammer.crafthammer.api.event.player.PlayerJoinEvent
import com.asledgehammer.sledgehammer.plugin.PluginModule

/**
 * **ExampleModule** TODO: Document.
 *
 * @author Jab
 */
class ExampleModule : PluginModule(), EventListener, LogListener {

  override fun onEnable() {
    addEventListener(this)
    // addLogListener(this)
  }

  // Example LogListener method.
  override fun onLogEntry(entry: LogEntry) {}

  @CommandHandler(commands = ["example"])
  fun onExampleCommand(command: CommandExecution) {
    val commander = command.commander
    if (commander is Player) {
      command.accept(message = "You are a player!")
    } else {
      command.accept(message = "You are the console!")
    }
  }

  @CommandHandler(
    commands = ["permissions", "p"],
    permission = ["example.permissions"]
  )
  fun onPermissionsCommand(execution: CommandExecution) {
    val cmd = execution.command
    val args = cmd.args
  }

  @EventHandler
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