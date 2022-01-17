@file:Suppress("MemberVisibilityCanBePrivate", "unused", "CanBeParameter")

package com.asledgehammer.framework.plugin

import com.asledgehammer.framework.cfg.CFGSection
import com.asledgehammer.framework.cfg.YamlFile
import com.asledgehammer.framework.event.Events
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.URL
import java.net.URLClassLoader
import java.util.*
import java.util.jar.JarEntry
import java.util.jar.JarFile

/**
 * TODO: Document.
 *
 * @author Jab
 *
 * @property file
 */
class Plugin(private val file: File) {

    /**
     * The internal ID to use throughout the framework.
     */
    val id: UUID = UUID.randomUUID()

    /**
     * TODO: Document.
     */
    val modules = HashMap<String, Module>()

    /**
     * TODO: Document.
     */
    lateinit var directory: File private set

    /**
     * TODO: Document.
     */
    lateinit var properties: Properties

    private val modulesToLoad = ArrayList<Module>()
    private val modulesLoaded = ArrayList<Module>()
    private val modulesToStart = ArrayList<Module>()
    private val modulesStarted = ArrayList<Module>()
    private val modulesStopped = ArrayList<Module>()
    private val modulesToUnload = ArrayList<Module>()
    private val modulesUnloaded = ArrayList<Module>()
    private var classLoader = this.javaClass.classLoader
    private var loadClasses = true

    fun init() {
        // create properties.
        try {
            val inputStream: InputStream = getResource("plugin.yml")
                ?: throw RuntimeException("plugin.yml is not found in the plugin: ${properties.name}")
            val cfg = YamlFile(null)
            cfg.read(inputStream)
            properties = Properties(cfg)
            inputStream.close()
            directory = File(Plugins.directory, properties.name + File.separator)
        } catch (e: IOException) {
            e.printStackTrace()
        }

        // create modules.
        try {
            if (loadClasses) classLoader = loadJarClasses(file)
            for ((key, value) in properties.modules) {
                modules[key] = instantiateModule(value)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    /**
     * TODO: Document.
     */
    fun getModule(name: String): Module? = modules[name]

    /**
     * TODO: Document.
     */
    @Suppress("UNCHECKED_CAST")
    fun <T : Module?> getModule(clazz: Class<out Module?>? = null): T? {
        var returned: Module? = null
        for (module: Module in modules.values) {
            if (module.javaClass == clazz) {
                returned = module
                break
            }
        }
        return returned as T?
    }

    /**
     * TODO: Document.
     */
    fun saveResource(path: String) {
        saveResourceAs(path, File(directory, path))
    }

    /**
     * Saves a resource from the Jar File to the destination path.
     *
     * @param path The String path in the Jar File.
     * @param destPath The String path to save to.
     */
    fun saveResourceAs(path: String, destPath: String) {
        saveResourceAs(path, File(destPath))
    }

    /**
     * Saves a resource from the Jar File to the destination path.
     *
     * @param path The String path in the Jar File.
     * @param dest The File destination to save to.
     */
    fun saveResourceAs(path: String, dest: File) {
        write(path, dest)
    }

    /**
     * Set the plug-in to load the classes in the Jar File. This is an option for embedded plug-ins.
     *
     * @param flag The Flag to set.
     */
    fun setLoadClasses(flag: Boolean) {
        loadClasses = flag
        if (!loadClasses) classLoader = ClassLoader.getSystemClassLoader()
    }

    /**
     * @param path The path of the resource to stream.
     *
     * @return Returns a InputStream for a registered File in the given Jar File.
     *
     * @throws IOException Thrown when the InputStream fails to establish.
     */
    fun getResource(path: String): InputStream? {
        return try {
            val classLoader = this.javaClass.classLoader
            val url: URL = classLoader.getResource(path) ?: return null
            val connection = url.openConnection()
            connection.useCaches = false
            connection.getInputStream()
        } catch (e: IOException) {
            null
        }
    }

    /**
     * TODO: Document.
     */
    internal fun load() {
        synchronized(this) {
            try {
                for (module in modulesToLoad) {
                    if (loadModule(module)) {
                        modulesToStart.add(module)
                        modulesLoaded.add(module)
                    }
                }
                modulesToLoad.clear()
            } catch (e: java.lang.Exception) {
                throw e
            }
        }
    }

    /**
     * TODO: Document.
     */
    internal fun startModules() {
        synchronized(this) {
            for (module in modulesToStart) if (startModule(module)) modulesStarted.add(module)
            modulesToStart.clear()
        }
    }

    /**
     * TODO: Document.
     */
    internal fun updateModules(delta: Long) {
        synchronized(this) {
            if (modulesToLoad.size > 0) load()
            if (modulesToStart.size > 0) startModules()
            for (module in modulesStarted) {
                if (!module.started) {
                    modulesStopped.add(module)
                    continue
                }
                updateModule(module, delta)
            }
            for (module in modulesStarted) if (!module.started) modulesStopped.add(module)
            for (module in modulesLoaded) if (!module.loaded) modulesUnloaded.add(module)
            if (modulesStopped.size > 0) {
                for (module in modulesStopped) modulesStarted.remove(module)
                modulesStopped.clear()
            }
            if (modulesUnloaded.size > 0) {
                for (module in modulesUnloaded) modulesLoaded.remove(module)
                modulesUnloaded.clear()
            }
        }
    }

    /**
     * TODO: Document.
     */
    internal fun stopModules() {
        synchronized(this) {
            for (module in modulesStarted) {
                if (module.loaded && module.started) {
                    stopModule(module)
                    modulesToUnload.add(module)
                }
            }
            modulesStarted.clear()
        }
    }

    /**
     * TODO: Document.
     */
    internal fun unloadModules() {
        synchronized(this) {
            for (module in modulesToUnload) if (module.loaded) unloadModule(module)
            modulesToUnload.clear()
            modules.clear()
        }
    }

    /**
     * TODO: Document.
     */
    internal fun addModule(module: Module) {
        val classLiteral: String = getClassLiteral(module.javaClass)
        for (moduleNext: Module in modulesToLoad) {
            val classLiteralNext: String = getClassLiteral(moduleNext.javaClass)
            if (classLiteral.equals(classLiteralNext, ignoreCase = true)) {
                throw IllegalArgumentException(
                    "Module $classLiteral is already loaded in the plug-in ${properties.name}."
                )
            }
        }
        modulesToLoad.add(module)
    }

    /**
     * TODO: Document.
     */
    internal fun removeModule(module: Module) {
        modules.remove(module.properties.name)
    }

    private fun instantiateModule(properties: Module.Properties): Module {
        try {
            val classToLoad = Class.forName(properties.location, true, classLoader)
            val module = classToLoad.newInstance() as Module
            module.properties = properties
            module.plugin = this
            module.directory = File(directory, module.properties.name + File.separator)
            modules[properties.name] = module
            modulesToLoad.add(module)
            return module
        } catch (e: Exception) {
            throw e
        }
    }

    private fun loadModule(module: Module): Boolean {
        if (module.started) {
            System.err.println("Module has already loaded and has started, and cannot be loaded.")
            return true
        }
        if (module.loaded) {
            System.err.println("Module has already loaded and cannot be loaded.")
            return true
        }
        try {
            println("Loading module ${module.properties.name}.")
            module.load()
            return true
        } catch (e: Exception) {
            System.err.println("Failed to load Module: ${module.properties.name}")
            e.printStackTrace(System.err)
        }
        return false
    }

    private fun startModule(module: Module): Boolean {
        if (!module.loaded) {
            System.err.println("Module ${module.properties.name} is not loaded and cannot be started.")
            return false
        }
        if (module.started) {
            System.err.println("Module ${module.properties.name} has already started.")
            return true
        }
        try {
            println("Starting module ${module.properties.name}.")
            module.start()
            return true
        } catch (e: Exception) {
            System.err.println("Failed to start Module: ${module.properties.name}")
            e.printStackTrace(System.err)
            if (module.loaded) unloadModule(module)
        }
        return false
    }

    private fun updateModule(module: Module, delta: Long): Boolean {
        try {
            module.update(delta)
            return true
        } catch (e: Exception) {
            System.err.println("Failed to update Module: ${module.properties.name}")
            e.printStackTrace(System.err)
            if (module.loaded) unloadModule(module)
        }
        return false
    }

    private fun stopModule(module: Module) {
        if (!module.loaded) {
            System.err.println("Module ${module.properties.name} is not loaded and cannot be stopped.")
            return
        }
        if (!module.started) {
            System.err.println("Module ${module.properties.name} has not started and cannot be stopped.")
            return
        }
        try {
            println("Stopping module ${module.properties.name}.")
            Events.unregister(module.id)
            module.stop()
        } catch (e: Exception) {
            System.err.println("Failed to stop Module: ${module.properties.name}")
            e.printStackTrace(System.err)
            if (module.loaded) unloadModule(module)
        }
    }

    private fun unloadModule(module: Module) {
        if (!module.loaded) {
            System.err.println("Module ${module.properties.name} is not loaded and cannot be unloaded.")
            return
        }
        try {
            if (module.started) stopModule(module)
            println("Unloading module ${module.properties.name}.")
            // Just in-case a module tries to register listeners between stopping and unloading.
            Events.unregister(module.id)
            module.unload()
        } catch (e: Exception) {
            System.err.println("Failed to unload Module: ${module.properties.name}")
            e.printStackTrace(System.err)
        }
    }

    /**
     * Writes a Jar File entry to a File.
     *
     * @param path The path to the resource in the jar.
     * @param destination The File destination to write the stream to.
     */
    private fun write(path: String, destination: File? = null) {
        try {
            val inputStream: InputStream = getResource(path) ?: return

            val os = if (destination == null) {
                FileOutputStream(File(directory, path))
            } else {
                FileOutputStream(destination)
            }

            val buffer = ByteArray(102400)
            var bytesRead: Int
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                os.write(buffer, 0, bytesRead)
            }

            inputStream.close()
            os.flush()
            os.close()
        } catch (e: Exception) {
            e.printStackTrace(System.err)
        }
    }

    companion object {

        /**
         * TODO: Document.
         */
        @Throws(IOException::class)
        private fun loadJarClasses(fileJar: File): ClassLoader {
            val url = fileJar.toURI().toURL()
            val urls = arrayOf(url)
            val loader: ClassLoader = URLClassLoader(urls)
            val listClasses: MutableList<String> = java.util.ArrayList()
            val jarFile = JarFile(fileJar)
            val e: Enumeration<*> = jarFile.entries()
            while (e.hasMoreElements()) {
                val entry = e.nextElement() as JarEntry
                if (entry.isDirectory || !entry.name.endsWith(".class")) {
                    continue
                }
                var className = entry.name.substring(0, entry.name.length - 6)
                className = className.replace('/', '.')
                listClasses.add(className)
            }
            jarFile.close()
            // Loads all classes in the JAR file.
            for (clazz in listClasses) {
                try {
                    loader.loadClass(clazz)
                } catch (error: ClassNotFoundException) {
                    System.err.println("Jar->Class not found: $clazz")
                    try {
                        ClassLoader.getSystemClassLoader().loadClass(clazz)
                    } catch (error2: Exception) {
                        System.err.println("System->Class not found: $clazz")
                    }
                } catch (error: NoClassDefFoundError) {
                    System.err.println("Jar->Class not found: $clazz")
                    try {
                        ClassLoader.getSystemClassLoader().loadClass(clazz)
                    } catch (error2: Exception) {
                        System.err.println("System->Class not found: $clazz")
                    }
                }
            }
            return loader
        }

        /**
         * TODO: Document.
         */
        fun getClassLiteral(clazz: Class<*>): String = "${clazz.getPackage().name}.${clazz.simpleName}"
    }

    /**
     * TODO: Document.
     *
     * @author Jab
     *
     * @property cfg
     */
    class Properties(val cfg: CFGSection) {

        /**
         * TODO: Document.
         */
        val modules = HashMap<String, Module.Properties>()

        /**
         * TODO: Document.
         */
        val name: String

        /**
         * TODO: Document.
         */
        val version: String

        /**
         * TODO: Document.
         */
        val description: String?

        init {
            require(cfg.isString("name")) { """The "name" field is not defined in the plugin.yml.""" }
            require(cfg.isString("version")) { """The "version" field is not defined in the plugin.yml.""" }
            require(cfg.isSection("modules")) { """The "modules" section is not defined in the plugin.yml.""" }

            name = cfg.getString("name")
            version = cfg.getString("version")

            val modulesCfg = cfg.getSection("modules")
            for (key in modulesCfg.allKeys) {
                require(modulesCfg.isSection(key)) { "The module \"$key\" failed to load. (Not a configured section)" }
                modules[key] = (Module.Properties(this, key, modulesCfg.getSection(key)))
            }

            description = if (cfg.isString("description")) {
                cfg.getString("description")
            } else {
                null
            }
        }
    }
}
