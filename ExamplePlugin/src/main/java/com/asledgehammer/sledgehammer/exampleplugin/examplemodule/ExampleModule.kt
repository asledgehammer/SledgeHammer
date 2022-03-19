@file:Suppress("unused")

package com.asledgehammer.sledgehammer.exampleplugin.examplemodule

import com.asledgehammer.crafthammer.api.event.EventHandler
import com.asledgehammer.crafthammer.api.Hammer
import com.asledgehammer.crafthammer.api.event.EventListener
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
class ExampleModule : Module(), EventListener {

  override fun onLoad() {
    println("ExampleModule.onLoad()")
  }

  override fun onEnable() {
    println("ExampleModule.onEnable()")
    Hammer.INSTANCE.events.register(id, this)
  }

  override fun onTick(delta: Long) {
  }

  override fun onDisable() {
    println("ExampleModule.onDisable()")
    Hammer.INSTANCE.events.unregister(id)
  }

  override fun onUnload() {
    println("ExampleModule.onUnload()")
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