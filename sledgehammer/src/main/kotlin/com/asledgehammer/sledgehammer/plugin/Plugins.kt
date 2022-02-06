@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package com.asledgehammer.sledgehammer.plugin

import com.asledgehammer.sledgehammer.Sledgehammer
import java.io.File

/**
 * **Plugins** TODO: Document.
 *
 * @author Jab
 */
object Plugins {

  /** TODO: Document. */
  val plugins = HashMap<String, Plugin>()

  /** TODO: Document. */
  var directory: File = File("plugins${File.separator}")

  private val pluginsToLoad = ArrayList<Plugin>()
  private val pluginsToStart = ArrayList<Plugin>()
  private val pluginsStarted = ArrayList<Plugin>()
  private val pluginsToUnload = ArrayList<Plugin>()
  private var timeThen = 0L

  /** TODO: Document. */
  @JvmStatic
  fun load(directory: File) {

    Plugins.directory = directory
    if (!Plugins.directory.exists()) {
      require(Plugins.directory.mkdirs()) { "Failed to create directory: \"${Plugins.directory.path}\"" }
    }

    var loadedModules = 0
    var loadedPlugins = 0
    val plugins = directory.listFiles()
    if (plugins != null && plugins.isNotEmpty()) {
      for (jar in plugins.filter { file -> file.isFile && file.extension.equals("jar", true) }) {
        try {
          require(jar.exists()) { "Jar file not found: ${jar.absolutePath}" }
          val plugin = Plugin(jar)
          plugin.init()
          pluginsToLoad.add(plugin)
          loadedModules += plugin.modules.size
          loadedPlugins++
        } catch (e: Exception) {
          e.printStackTrace()
        }
      }
    }

    if (loadedPlugins == 1) Sledgehammer.log("Loaded $loadedPlugins plugin.")
    else Sledgehammer.log("Loaded $loadedPlugins plugins.")

    if (loadedModules == 1) Sledgehammer.log("Registered $loadedModules module.")
    else Sledgehammer.log("Registered $loadedModules modules.")

    for (plugin in pluginsToLoad) {
      Sledgehammer.log("Loading plugin ${plugin.properties.name}'s module(s):")
      plugin.load()
      pluginsToStart.add(plugin)
    }

    pluginsToLoad.clear()
  }

  /** TODO: Document. */
  @JvmStatic
  fun enable() {
    for (plugin in pluginsToStart) {
      Sledgehammer.log("Starting plugin ${plugin.properties.name}'s module(s):")
      plugin.startModules()
      pluginsStarted.add(plugin)
      plugins[plugin.properties.name] = plugin
    }
    pluginsToStart.clear()
  }

  /** TODO: Document. */
  @JvmStatic
  fun tick() {
    val timeNow = System.currentTimeMillis()
    val delta = if (timeThen != 0L) timeNow - timeThen else 0L
    for (plugin in pluginsStarted) {
      plugin.updateModules(delta)
    }
    timeThen = timeNow
  }

  /** TODO: Document. */
  @JvmStatic
  fun disable() {
    for (plugin in pluginsStarted) {
      Sledgehammer.log("Stopping ${plugin.properties.name}'s module(s):")
      plugin.stopModules()
      pluginsToUnload.add(plugin)
    }
    pluginsStarted.clear()
  }

  /** TODO: Document. */
  @JvmStatic
  fun unload() {
    for (plugin in pluginsToUnload) {
      Sledgehammer.log("Unloading ${plugin.properties.name}'s module(s):")
      plugin.unloadModules()
    }
    pluginsToUnload.clear()
  }

  /** TODO: Document. */
  @JvmStatic
  fun clear() {
    plugins.clear()
  }
}
