@file:Suppress("unused")

package com.asledgehammer.sledgehammer.exampleplugin.examplemodule

import com.asledgehammer.crafthammer.api.Hammer
import com.asledgehammer.crafthammer.api.event.EventHandler
import com.asledgehammer.crafthammer.api.event.EventListener
import com.asledgehammer.crafthammer.api.event.log.LogEntry
import com.asledgehammer.crafthammer.api.event.log.LogListener
import com.asledgehammer.crafthammer.api.event.network.PostLoginEvent
import com.asledgehammer.crafthammer.api.event.network.PreLoginEvent
import com.asledgehammer.crafthammer.api.event.player.PlayerJoinEvent
import com.asledgehammer.crafthammer.api.event.player.PlayerQuitEvent
import com.asledgehammer.sledgehammer.plugin.Module

/**
 * **ExampleModule** TODO: Document.
 *
 * @author Jab
 */
class ExampleModule : Module(), EventListener, LogListener {

  override fun onLoad() {
    println("ExampleModule.onLoad()")
  }

  override fun onEnable() {
    println("ExampleModule.onEnable()")
    Hammer.instance!!.events.register(id, this)
    Hammer.instance!!.addLogListener(id, this)
  }

  override fun onTick(delta: Long) {
  }

  override fun onDisable() {
    println("ExampleModule.onDisable()")
    Hammer.instance!!.events.unregister(id)
    Hammer.instance!!.removeLogListener(id, this)
  }

  override fun onUnload() {
    println("ExampleModule.onUnload()")
  }

  override fun onLogMessage(entry: LogEntry) {
    println(entry)
  }

  @EventHandler
  fun on(event: PreLoginEvent) {
    println("ExampleModule.on(PreLoginEvent)")
    println(event)
  }

  @EventHandler
  fun on(event: PostLoginEvent) {
    println("ExampleModule.on(PostLoginEvent)")
    println(event)
  }

  @EventHandler
  fun on(event: PlayerJoinEvent) {
    println("ExampleModule.on(PlayerJoinEvent)")
    println(event)
  }

  @EventHandler
  fun on(event: PlayerQuitEvent) {
    println("ExampleModule.on(PlayerQuitEvent)")
    println(event)
  }
}