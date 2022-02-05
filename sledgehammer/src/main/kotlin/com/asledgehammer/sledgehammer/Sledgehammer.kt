package com.asledgehammer.sledgehammer

import com.asledgehammer.framework.event.Events
import com.asledgehammer.framework.plugin.Plugins
import com.asledgehammer.framework.task.TaskManager
import zombie.debug.DebugLog
import zombie.debug.DebugType
import java.io.File

object Sledgehammer {

  var debug: Boolean = false

  @JvmStatic
  fun init() {
    Plugins.load(File("plugins" + File.separator))
  }

  @JvmStatic
  fun start() {
    Plugins.start()
  }

  @JvmStatic
  fun update() {
    Plugins.update()
    TaskManager.tick()
  }

  @JvmStatic
  fun stop() {
    TaskManager.tick()
    Plugins.stop()
    Events.reset()
  }

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
  fun addDefaultPermission(node: String, flag: Boolean) {
    TODO("Not yet implemented")
  }
}
