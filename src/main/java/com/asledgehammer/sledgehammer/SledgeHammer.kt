package com.asledgehammer.sledgehammer

import com.asledgehammer.crafthammer.CraftHammer
import com.asledgehammer.craftnail.hook.CraftHook
import com.asledgehammer.sledgehammer.api.Plugins
import com.asledgehammer.sledgehammer.plugin.CraftPlugins
import java.io.File

class SledgeHammer : CraftHook() {

  val plugins: Plugins get() = Plugins.instance!!

  override fun onLoad(): Boolean {
    Plugins.instance = CraftPlugins()
    plugins.load(File("plugins${File.separator}"))
    return true
  }

  override fun onEnable(): Boolean {
    plugins.enable()
    return true
  }

  override fun onTick(delta: Long) {
    plugins.tick()
  }

  override fun onDisable() {
    plugins.disable()
  }

  override fun onUnload() {
    plugins.unload()
    plugins.clear()
  }

  override fun getId(): String = "sledgehammer"

  companion object {

    var DEBUG: Boolean = false

    /** TODO: Document. */
    @JvmStatic
    fun log(vararg objects: Any?) = CraftHammer.log(*objects)

    /** TODO: Document. */
    @JvmStatic
    fun log(list: Iterable<Any?>?) = CraftHammer.log(list)

    /** TODO: Document. */
    @JvmStatic
    @JvmOverloads
    fun logError(message: String, cause: Throwable? = null) = CraftHammer.logError(message, cause)
  }
}
