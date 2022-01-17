package com.asledgehammer.sledgehammer

import com.asledgehammer.framework.event.Events
import com.asledgehammer.framework.plugin.Plugins
import com.asledgehammer.framework.task.TaskManager
import java.io.File

object Sledgehammer {

    var file = File(Sledgehammer::class.java.protectionDomain.codeSource.location.toURI().path)
    var debug: Boolean = false

    fun init() {
        println("SLEDGEHAMMER INIT")
        Plugins.load(File("plugins" + File.separator))
    }

    fun start() {
        println("SLEDGEHAMMER START")
        Plugins.start()
    }

    fun update() {
        Plugins.update()
        TaskManager.tick()
    }

    fun stop() {
        println("SLEDGEHAMMER STOP")
        Plugins.stop()
        Events.reset()
    }

    fun addDefaultPermission(node: String, flag: Boolean) {
        TODO("Not yet implemented")
    }
}
