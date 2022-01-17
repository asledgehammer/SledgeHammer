@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package com.asledgehammer.framework.plugin

import java.io.File

/**
 * TODO: Document.
 *
 * @author Jab
 */
object Plugins {

    /**
     * TODO: Document.
     */
    val plugins = HashMap<String, Plugin>()

    /**
     * TODO: Document.
     */
    var directory: File = File("plugins${File.separator}")

    private val pluginsToLoad = ArrayList<Plugin>()
    private val pluginsToStart = ArrayList<Plugin>()
    private val pluginsStarted = ArrayList<Plugin>()
    private val pluginsToUnload = ArrayList<Plugin>()
    private var timeThen = 0L

    /**
     * TODO: Document.
     */
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

        if (loadedPlugins == 1) println("Loaded $loadedPlugins plugin.")
        else println("Loaded $loadedPlugins plugins.")

        if (loadedModules == 1) println("Registered $loadedModules module.")
        else println("Registered $loadedModules modules.")

        for (plugin in pluginsToLoad) {
            println("Loading plugin ${plugin.properties.name}'s modules:")
            plugin.load()
            pluginsToStart.add(plugin)
        }

        pluginsToLoad.clear()
    }

    /**
     * TODO: Document.
     */
    fun start() {
        for (plugin in pluginsToStart) {
            println("Starting plugin ${plugin.properties.name}'s modules:")
            plugin.startModules()
            pluginsStarted.add(plugin)
            plugins[plugin.properties.name] = plugin
        }
        pluginsToStart.clear()
    }

    /**
     * TODO: Document.
     */
    fun update() {
        val timeNow = System.currentTimeMillis()
        val delta = if (timeThen != 0L) timeNow - timeThen else 0L
        for (plugin in pluginsStarted) {
            plugin.updateModules(delta)
        }
        timeThen = timeNow
    }

    /**
     * TODO: Document.
     */
    fun stop() {
        for (plugin in pluginsStarted) {
            println("Stopping ${plugin.properties.name}'s modules:")
            plugin.stopModules()
            pluginsToUnload.add(plugin)
        }
        pluginsStarted.clear()
        plugins.clear()
        for (plugin in pluginsToUnload) {
            println("Unloading ${plugin.properties.name}'s modules:")
            plugin.unloadModules()
        }
        pluginsToUnload.clear()
    }
}
