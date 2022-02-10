@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package com.asledgehammer.sledgehammer.plugin

import com.asledgehammer.crafthammer.util.cfg.CFGSection
import com.asledgehammer.sledgehammer.Sledgehammer
import java.io.File
import java.util.*

/**
 * **Module** TODO: Document.
 *
 * @author Jab
 */
open class Module {

  /**
   * The internal ID to use throughout the framework.
   */
  val id: UUID = UUID.randomUUID()

  /** TODO: Document. */
  lateinit var directory: File internal set

  /** TODO: Document. */
  lateinit var plugin: Plugin internal set

  /** TODO: Document. */
  lateinit var properties: Properties internal set

  /** TODO: Document. */
  var loaded: Boolean = false
    internal set

  /** TODO: Document. */
  var enabled: Boolean = false
    internal set

  /**
   * @return Returns true if the Module loads successfully.
   */
  internal fun load(): Boolean {
    try {
      onLoad()
      loaded = true
      return true
    } catch (e: Exception) {
      Sledgehammer.logError("Failed to load module.", e)
      loaded = false
    }
    return false
  }

  /** TODO: Document. */
  internal fun enable() {
    if (!enabled) {
      onEnable()
      enabled = true
    } else {
      Sledgehammer.logError("Module is already enabled.")
    }
  }

  /**
   * TODO: Document.
   *
   * @param delta The latency in milliseconds since the last tick.
   */
  internal fun tick(delta: Long) {
    if (enabled) onTick(delta)
  }

  /**
   * TODO: Document.
   *
   * @return Returns true if the Module stopped successfully.
   */
  internal fun disable(): Boolean {
    if (loaded) {
      try {
        if (enabled) {
          this.onDisable()
          enabled = false
        } else {
          Sledgehammer.logError("Module is not enabled.")
        }
      } catch (e: Exception) {
        Sledgehammer.logError("Failed to safely disable module.", e)
      }
    }
    return true
  }

  /**
   * TODO: Document.
   *
   * @return Returns true if the Module unloads successfully.
   */
  internal fun unload(): Boolean {
    try {
      if (loaded) {
        this.onUnload()
        this.loaded = false
      }
    } catch (e: Exception) {
      Sledgehammer.logError("Failed to safely unload module.", e)
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

  /**
   * @param path The path to the File inside the Jar File.
   * @param overwrite The flag to set if the File is to be overwritten, regardless of it's state.
   */
  @JvmOverloads
  fun saveResource(path: String, overwrite: Boolean = false) {
    val file = File(directory, path)
    if (!overwrite && file.exists()) return
    plugin.saveResourceAs(path, file)
  }

  /** TODO: Document. */
  @JvmOverloads
  fun saveResourceAs(path: String, filePath: String, overwrite: Boolean = false) {
    val file = File(directory, filePath)
    if (!overwrite && file.exists()) return
    plugin.saveResourceAs(path, file)
  }

  /** TODO: Document. */
  @JvmOverloads
  fun saveResourceAs(path: String, filePath: File, overwrite: Boolean = false) {
    if (!overwrite && filePath.exists()) return
    plugin.saveResourceAs(path, filePath)
  }

  /**
   * TODO: Document.
   *
   * @author Jab
   *
   * @property plugin
   * @property name
   * @property cfg
   */
  class Properties(val plugin: Plugin.Properties, val name: String, val cfg: CFGSection) {

    /** TODO: Document. */
    val version: String

    /** TODO: Document. */
    val location: String

    /** TODO: Document. */
    val description: String?

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
