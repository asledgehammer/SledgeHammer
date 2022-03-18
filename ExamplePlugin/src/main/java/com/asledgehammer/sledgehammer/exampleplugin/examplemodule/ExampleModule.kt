@file:Suppress("unused")

package com.asledgehammer.sledgehammer.exampleplugin.examplemodule

import com.asledgehammer.sledgehammer.plugin.Module

class ExampleModule : Module() {

  override fun onLoad() {
    println("ExampleModule.onLoad()")
  }

  override fun onEnable() {
    println("ExampleModule.onEnable()")
  }

  override fun onTick(delta: Long) {
  }

  override fun onDisable() {
    println("ExampleModule.onDisable()")
  }

  override fun onUnload() {
    println("ExampleModule.onUnload()")
  }
}