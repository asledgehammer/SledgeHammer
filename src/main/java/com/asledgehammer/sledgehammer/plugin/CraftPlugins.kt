@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package com.asledgehammer.sledgehammer.plugin

import com.asledgehammer.sledgehammer.SledgeHammer
import com.asledgehammer.sledgehammer.api.Module
import com.asledgehammer.sledgehammer.api.Plugin
import com.asledgehammer.sledgehammer.api.Plugins
import java.io.File

/**
 * **Plugins** TODO: Document.
 *
 * @author Jab
 */
class CraftPlugins: Plugins {

  override val plugins = HashMap<String, Plugin>()
  override var directory: File = File("plugins${File.separator}")

  private val pluginsToLoad = ArrayList<CraftPlugin>()
  private val pluginsToEnable = ArrayList<CraftPlugin>()
  private val pluginsEnabled = ArrayList<CraftPlugin>()
  private val pluginsToUnload = ArrayList<CraftPlugin>()
  private var timeThen = 0L

  override fun load(directory: File) {

    this.directory = directory
    if (!this.directory.exists()) {
      require(this.directory.mkdirs()) { "Failed to create directory: \"${this.directory.path}\"" }
    }

    var loadedModules = 0
    var loadedPlugins = 0
    val plugins = directory.listFiles()
    if (plugins != null && plugins.isNotEmpty()) {
      for (jar in plugins.filter { file -> file.isFile && file.extension.equals("jar", true) }) {
        try {
          require(jar.exists()) { "Jar file not found: ${jar.absolutePath}" }
          val plugin = CraftPlugin(jar)
          plugin.init()
          pluginsToLoad.add(plugin)
          loadedModules += plugin.modules.size
          loadedPlugins++
        } catch (e: Exception) {
          e.printStackTrace()
        }
      }
    }

    if (loadedPlugins == 1) SledgeHammer.log("Loaded $loadedPlugins plugin.")
    else SledgeHammer.log("Loaded $loadedPlugins plugins.")

    if (loadedModules == 1) SledgeHammer.log("Registered $loadedModules module.")
    else SledgeHammer.log("Registered $loadedModules modules.")

    for (plugin in pluginsToLoad) {
      SledgeHammer.log("Loading plugin ${plugin.properties.name}'s module(s):")
      plugin.load()
      pluginsToEnable.add(plugin)
    }

    pluginsToLoad.clear()
  }

  override fun enable() {
    for (plugin in pluginsToEnable) {
      SledgeHammer.log("Enabling plugin ${plugin.properties.name}'s module(s):")
      plugin.enableModules()
      pluginsEnabled.add(plugin)
      plugins[plugin.properties.name] = plugin
    }
    pluginsToEnable.clear()
  }

  override fun tick() {
    val timeNow = System.currentTimeMillis()
    val delta = if (timeThen != 0L) timeNow - timeThen else 0L
    for (plugin in pluginsEnabled) {
      plugin.tickModules(delta)
    }
    timeThen = timeNow
  }

  override fun disable() {
    for (plugin in pluginsEnabled) {
      SledgeHammer.log("Disabling ${plugin.properties.name}'s module(s):")
      plugin.disableModules()
      pluginsToUnload.add(plugin)
    }
    pluginsEnabled.clear()
  }

  override fun unload() {
    for (plugin in pluginsToUnload) {
      SledgeHammer.log("Unloading ${plugin.properties.name}'s module(s):")
      plugin.unloadModules()
    }
    pluginsToUnload.clear()
  }

  override fun clear() {
    plugins.clear()
  }

  @Suppress("UNCHECKED_CAST")
  override fun <T : Plugin?> getPlugin(clazz: Class<out Plugin?>): T? {
    var returned: Plugin? = null
    for ((_, plugin) in plugins) {
      if (plugin.javaClass == clazz) {
        returned = plugin
        break
      }
    }
    return if(returned != null) returned as T else null
  }
}
