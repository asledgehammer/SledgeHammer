@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package com.asledgehammer.sledgehammer.plugin

import com.asledgehammer.crafthammer.CraftHammer
import com.asledgehammer.crafthammer.api.event.log.LogListener
import com.asledgehammer.crafthammer.util.cfg.CFGSection
import com.asledgehammer.craftnail.CraftNail
import com.asledgehammer.sledgehammer.SledgeHammer
import com.asledgehammer.sledgehammer.api.Module
import com.asledgehammer.sledgehammer.api.Plugin
import java.io.File
import java.util.*

/**
 * **Module** TODO: Document.
 *
 * @author Jab
 */
open class CraftModule : Module {

  internal lateinit var _plugin: Plugin
  internal lateinit var _directory: File
  internal lateinit var _properties: Module.Properties
  internal var _loaded: Boolean = false
  internal var _enabled: Boolean = false

  override val id: UUID = UUID.randomUUID()
  override val plugin: Plugin get() = _plugin
  override val directory: File get() = _directory
  override val properties: Module.Properties get() = _properties
  override val loaded: Boolean get() = _loaded
  override val enabled: Boolean get() = _enabled

  /** @return Returns true if the Module loads successfully. */
  internal fun load(): Boolean {
    try {
      onLoad()
      _loaded = true
      return true
    } catch (e: Exception) {
      SledgeHammer.logError("Failed to load module.", e)
      _loaded = false
    }
    return false
  }

  internal fun enable() {
    if (!enabled) {
      onEnable()
      _enabled = true
    } else {
      SledgeHammer.logError("Module is already enabled.")
    }
  }

  internal fun tick(delta: Long) {
    if (enabled) onTick(delta)
  }

  internal fun disable(): Boolean {
    if (loaded) {
      try {
        if (enabled) {
          onDisable()
          _enabled = false
        } else {
          SledgeHammer.logError("Module is not enabled.")
        }
      } catch (e: Exception) {
        SledgeHammer.logError("Failed to safely disable module.", e)
      }
    }
    return true
  }

  internal fun unload(): Boolean {
    try {
      if (loaded) {
        onUnload()
        _loaded = false
      }
    } catch (e: Exception) {
      SledgeHammer.logError("Failed to safely unload module.", e)
    }
    return true
  }

  /** Fired when the Module is loaded. */
  protected open fun onLoad() {}

  /** Fired when the Module is enabled. */
  protected open fun onEnable() {}

  /**
   * Fired when the Module is updated.
   *
   * @param delta The delta in milliseconds since the last update.
   */
  protected open fun onTick(delta: Long) {}

  /** Fired when the Module is disabled. */
  protected open fun onDisable() {}

  /** Fired when the Module is unloaded. */
  protected open fun onUnload() {}


  override fun saveResource(path: String, overwrite: Boolean) {
    val file = File(directory, path)
    if (!overwrite && file.exists()) return
    plugin.saveResourceAs(path, file)
  }

  override fun saveResourceAs(path: String, filePath: String, overwrite: Boolean) {
    val file = File(directory, filePath)
    if (!overwrite && file.exists()) return
    plugin.saveResourceAs(path, file)
  }

  override fun saveResourceAs(path: String, filePath: File, overwrite: Boolean) {
    if (!overwrite && filePath.exists()) return
    plugin.saveResourceAs(path, filePath)
  }

  override fun log(list: List<Any?>) {
    CraftNail.log(list)
  }

  override fun log(vararg objects: Any?) {
    CraftNail.log(*objects)
  }

  override fun logError(message: String, cause: Throwable?) {
    CraftNail.logError(message, cause)
  }

  override fun addLogListener(listener: LogListener) {
    CraftHammer.addLogListener(id, listener)
  }

  override fun removeLogListener(listener: LogListener) {
    CraftHammer.removeLogListener(id, listener)
  }

  override fun removeLogListeners() {
    CraftHammer.removeLogListeners(id)
  }

  class CraftProperties(
    override val plugin: Plugin.Properties,
    override val name: String,
    override val cfg: CFGSection,
  ) : Module.Properties {

    override val version: String
    override val location: String
    override val description: String?

    init {
      require(cfg.isString("version")) { "The \"version\" field is not defined in the module." }
      require(cfg.isString("class")) { "The \"class\" field is not defined in the module." }
      version = cfg.getString("version")
      description = if (cfg.isString("description")) {
        cfg.getString("description")
      } else {
        null
      }
      location = if (cfg.isString("class")) {
        cfg.getString("class")
      } else {
        "unknown"
      }
    }
  }
}
