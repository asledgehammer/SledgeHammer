package com.asledgehammer.sledgehammer

import com.asledgehammer.craftboid.api.CraftHook
import com.asledgehammer.craftboid.util.ANSIUtils
import com.asledgehammer.sledgehammer.event.Events
import com.asledgehammer.sledgehammer.plugin.Plugins
import com.asledgehammer.sledgehammer.task.Tasks
import zombie.debug.DebugLog
import zombie.debug.DebugType
import java.io.File

class Sledgehammer : CraftHook() {

  var debug: Boolean = false

  override fun onLoad(): Boolean {
    Plugins.load(File("plugins" + File.separator))
    return true
  }

  override fun onEnable(): Boolean {
    Plugins.enable()
    return true
  }

  override fun onTick(delta: Long) {
    Plugins.tick()
    Tasks.tick()
  }

  override fun onDisable() {
    Tasks.tick()
    Plugins.disable()
  }

  override fun onUnload() {
    Events.reset()
    Plugins.unload()
    Plugins.clear()
  }

  override fun getId(): String = "sledgehammer"

  companion object {
    @JvmStatic
    fun log(vararg objects: Any?) {
      if (objects.isEmpty()) {
        DebugLog.log(DebugType.Sledgehammer, "")
      } else {
        for (next in objects) {
          if (next == null) {
            DebugLog.log(DebugType.Sledgehammer, "")
          } else {
            DebugLog.log(DebugType.Sledgehammer, next.toString())
          }
        }
      }
    }

    @JvmStatic
    fun log(list: Iterable<Any?>?) {
      if (list == null) {
        DebugLog.log(DebugType.Sledgehammer, "")
      } else {
        for (next in list) {
          if (next == null) {
            DebugLog.log(DebugType.Sledgehammer, "")
          } else {
            DebugLog.log(DebugType.Sledgehammer, next.toString())
          }
        }
      }
    }

    @JvmStatic
    @JvmOverloads
    fun logError(message: String, throwable: Throwable? = null) {

      val red = ANSIUtils.ANSI_BRIGHT_RED
      val reset = ANSIUtils.ANSI_RESET

      DebugLog.log(DebugType.Sledgehammer, "$red$message$reset")

      if (throwable != null) {
        DebugLog.log(DebugType.Sledgehammer, "$red${throwable.javaClass.name}: ${throwable.message}$reset")
        for (elm in throwable.stackTrace) {
          DebugLog.log(DebugType.Sledgehammer, "$red at ${elm}$reset")
        }

        val cause = throwable.cause
        if (cause != null) {
          DebugLog.log(DebugType.Sledgehammer,
            "$red Caused by: ${throwable.javaClass.name}: ${cause.message}$reset")
          for (elm in cause.stackTrace) {
            DebugLog.log(DebugType.Sledgehammer, " at $red${elm}$reset")
          }
        }
      }
    }
  }
}
