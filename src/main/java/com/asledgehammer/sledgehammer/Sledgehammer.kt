package com.asledgehammer.sledgehammer

import com.asledgehammer.crafthammer.CraftHammer
import com.asledgehammer.craftnail.hook.CraftHook
import com.asledgehammer.sledgehammer.plugin.Plugins
import java.io.File

class Sledgehammer : CraftHook() {

  var debug: Boolean = false

  override fun onLoad(): Boolean {
    Plugins.load(File("plugins${File.separator}"))
    return true
  }

  override fun onEnable(): Boolean {
    Plugins.enable()
    return true
  }

  override fun onTick(delta: Long) {
    Plugins.tick()
  }

  override fun onDisable() {
    Plugins.disable()
  }

  override fun onUnload() {
    Plugins.unload()
    Plugins.clear()
  }

  override fun getId(): String = "sledgehammer"

  companion object {
    /** TODO: Document. */
    @JvmStatic
    fun log(vararg objects: Any?) = CraftHammer.log(objects)

    /** TODO: Document. */
    @JvmStatic
    fun log(list: Iterable<Any?>?) = CraftHammer.log(list)

    /** TODO: Document. */
    @JvmStatic
    @JvmOverloads
    fun logError(message: String, throwable: Throwable? = null) = CraftHammer.logError(message, throwable)
  }
}
