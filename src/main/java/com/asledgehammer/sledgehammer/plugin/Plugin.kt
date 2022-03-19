@file:Suppress("MemberVisibilityCanBePrivate", "unused", "CanBeParameter")

package com.asledgehammer.sledgehammer.plugin

import com.asledgehammer.crafthammer.api.Hammer
import com.asledgehammer.crafthammer.util.cfg.YamlFile
import com.asledgehammer.crafthammer.util.cfg.CFGSection
import com.asledgehammer.sledgehammer.Sledgehammer
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.URLClassLoader
import java.util.*
import java.util.jar.JarEntry
import java.util.jar.JarFile

/**
 * **Plugin** TODO: Document.
 *
 * @author Jab
 *
 * @property file
 */
class Plugin(private val file: File) {

  val jarFile = JarFile(file)

  /**
   * The internal ID to use throughout the framework.
   */
  val id: UUID = UUID.randomUUID()

  /** TODO: Document. */
  val modules = HashMap<String, Module>()

  /** TODO: Document. */
  lateinit var directory: File private set

  /** TODO: Document. */
  lateinit var properties: Properties

  private val modulesToLoad = ArrayList<Module>()
  private val modulesLoaded = ArrayList<Module>()
  private val modulesToEnable = ArrayList<Module>()
  private val modulesEnabled = ArrayList<Module>()
  private val modulesDisabled = ArrayList<Module>()
  private val modulesToUnload = ArrayList<Module>()
  private val modulesUnloaded = ArrayList<Module>()
  private var classLoader = this.javaClass.classLoader
  private var loadClasses = true

  fun init() {
    // create properties.
    try {
      val inputStream: InputStream = getResource("plugin.yml")
        ?: throw RuntimeException("plugin.yml is not found in the plugin: ${file.name}")
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

  /** TODO: Document. */
  fun getModule(name: String): Module? = modules[name]

  /** TODO: Document. */
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

  /** TODO: Document. */
  fun saveResource(path: String) {
    saveResourceAs(path, File(directory, path))
  }

  /**
   * Saves a resource from the Jar File to the destination path.
   *
   * @param path The String path in the Jar File.
   * @param dstPath The String path to save to.
   */
  fun saveResourceAs(path: String, dstPath: String) {
    saveResourceAs(path, File(dstPath))
  }

  /**
   * Saves a resource from the Jar File to the destination path.
   *
   * @param path The String path in the Jar File.
   * @param dst The File destination to save to.
   */
  fun saveResourceAs(path: String, dst: File) {
    write(path, dst)
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
    try {

      val entry = this.jarFile.getEntry(path) ?: return null
      return this.jarFile.getInputStream(entry)

      //      val url: URL? = this.javaClass.classLoader.getResource(path)
      //      if (url == null) {
      //        println("url is null.")
      //        null
      //      } else {
      //        val connection = url.openConnection()
      //        connection.useCaches = false
      //        connection.getInputStream()
      //      }
    } catch (e: IOException) {
      e.printStackTrace()
      return null
    }
  }

  /** TODO: Document. */
  internal fun load() {
    synchronized(this) {
      try {
        for (module in modulesToLoad) {
          if (loadModule(module)) {
            modulesToEnable.add(module)
            modulesLoaded.add(module)
          }
        }
        modulesToLoad.clear()
      } catch (e: java.lang.Exception) {
        throw e
      }
    }
  }

  /** TODO: Document. */
  internal fun enableModules() {
    synchronized(this) {
      for (module in modulesToEnable) if (enableModule(module)) modulesEnabled.add(module)
      modulesToEnable.clear()
    }
  }

  /** TODO: Document. */
  internal fun tickModules(delta: Long) {
    synchronized(this) {
      if (modulesToLoad.size > 0) load()
      if (modulesToEnable.size > 0) enableModules()
      for (module in modulesEnabled) {
        if (!module.enabled) {
          modulesDisabled.add(module)
          continue
        }
        tickModule(module, delta)
      }
      for (module in modulesEnabled) if (!module.enabled) modulesDisabled.add(module)
      for (module in modulesLoaded) if (!module.loaded) modulesUnloaded.add(module)
      if (modulesDisabled.size > 0) {
        for (module in modulesDisabled) modulesEnabled.remove(module)
        modulesDisabled.clear()
      }
      if (modulesUnloaded.size > 0) {
        for (module in modulesUnloaded) modulesLoaded.remove(module)
        modulesUnloaded.clear()
      }
    }
  }

  /** TODO: Document. */
  internal fun disableModules() {
    synchronized(this) {
      for (module in modulesEnabled) {
        if (module.loaded && module.enabled) {
          disableModule(module)
          modulesToUnload.add(module)
        }
      }
      modulesEnabled.clear()
    }
  }

  /** TODO: Document. */
  internal fun unloadModules() {
    synchronized(this) {
      for (module in modulesToUnload) if (module.loaded) unloadModule(module)
      modulesToUnload.clear()
      modules.clear()
    }
  }

  /** TODO: Document. */
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

  /** TODO: Document. */
  internal fun removeModule(module: Module) {
    modules.remove(module.properties.name)
  }

  private fun instantiateModule(properties: Module.Properties): Module {
    try {
      val classToLoad = Class.forName(properties.location, true, classLoader)
      val module = classToLoad.getConstructor().newInstance() as Module
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
    if (module.enabled) {
      Sledgehammer.logError("Module has already loaded and has enabled, and cannot be loaded.")
      return true
    }
    if (module.loaded) {
      Sledgehammer.logError("Module has already loaded and cannot be loaded.")
      return true
    }
    try {
      Sledgehammer.log("Loading module ${module.properties.name}.")
      module.load()
      return true
    } catch (e: Exception) {
      Sledgehammer.logError("Failed to load Module: ${module.properties.name}", e)
    }
    return false
  }

  private fun enableModule(module: Module): Boolean {
    if (!module.loaded) {
      Sledgehammer.logError("Module ${module.properties.name} is not loaded and cannot be enabled.")
      return false
    }
    if (module.enabled) {
      Sledgehammer.logError("Module ${module.properties.name} has already enabled.")
      return true
    }
    try {
      Sledgehammer.log("Enabling module ${module.properties.name}.")
      module.enable()
      return true
    } catch (e: Exception) {
      Sledgehammer.logError("Failed to enable Module: ${module.properties.name}", e)
      if (module.loaded) unloadModule(module)
    }
    return false
  }

  private fun tickModule(module: Module, delta: Long): Boolean {
    try {
      module.tick(delta)
      return true
    } catch (e: Exception) {
      Sledgehammer.logError("Failed to tick Module: ${module.properties.name}", e)
      if (module.loaded) unloadModule(module)
    }
    return false
  }

  private fun disableModule(module: Module) {
    if (!module.loaded) {
      Sledgehammer.logError("Module ${module.properties.name} is not loaded and cannot be disabled.")
      return
    }
    if (!module.enabled) {
      Sledgehammer.logError("Module ${module.properties.name} has not enabled and cannot be disabled.")
      return
    }
    try {
      Sledgehammer.log("Disabling module ${module.properties.name}.")
      Hammer.instance!!.events.unregister(module.id)
      module.disable()
    } catch (e: Exception) {
      Sledgehammer.logError("Failed to disable Module: ${module.properties.name}", e)
      if (module.loaded) unloadModule(module)
    }
  }

  private fun unloadModule(module: Module) {
    if (!module.loaded) {
      Sledgehammer.logError("Module ${module.properties.name} is not loaded and cannot be unloaded.")
      return
    }
    try {
      if (module.enabled) disableModule(module)
      Sledgehammer.log("Unloading module ${module.properties.name}.")
      // Just in-case a module tries to register listeners between stopping and unloading.
      Hammer.instance!!.events.unregister(module.id)
      module.unload()
    } catch (e: Exception) {
      Sledgehammer.logError("Failed to unload Module: ${module.properties.name}", e)
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

      val os = if (destination == null) FileOutputStream(File(directory, path))
      else FileOutputStream(destination)

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

  fun log(vararg objects: Any) {
    Sledgehammer.log(*objects)
  }

  fun logError(message: String, cause: Throwable? = null) {
    Sledgehammer.logError(message, cause)
  }

  companion object {

    /** TODO: Document. */
    @Throws(IOException::class)
    private fun loadJarClasses(fileJar: File): ClassLoader {
      val url = fileJar.toURI().toURL()
      val urls = arrayOf(url)
      val loader: ClassLoader = URLClassLoader(urls)
      val listClasses = ArrayList<String>()
      val jarFile = JarFile(fileJar)
      val e: Enumeration<*> = jarFile.entries()
      while (e.hasMoreElements()) {
        val entry = e.nextElement() as JarEntry
        if (entry.isDirectory || !entry.name.endsWith(".class")) continue
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
          Sledgehammer.logError("Jar->Class not found: $clazz")
          try {
            ClassLoader.getSystemClassLoader().loadClass(clazz)
          } catch (error2: Exception) {
            Sledgehammer.logError("System->Class not found: $clazz")
          }
        } catch (error: NoClassDefFoundError) {
          Sledgehammer.logError("Jar->Class not found: $clazz")
          try {
            ClassLoader.getSystemClassLoader().loadClass(clazz)
          } catch (error2: Exception) {
            Sledgehammer.logError("System->Class not found: $clazz")
          }
        }
      }
      return loader
    }

    /** TODO: Document. */
    fun getClassLiteral(clazz: Class<*>): String = "${clazz.getPackage().name}.${clazz.simpleName}"
  }

  /**
   * **Properties** TODO: Document.
   *
   * @author Jab
   *
   * @property cfg
   */
  class Properties(val cfg: CFGSection) {

    /** TODO: Document. */
    val modules = HashMap<String, Module.Properties>()

    /** TODO: Document. */
    val name: String

    /** TODO: Document. */
    val version: String

    /** TODO: Document. */
    val description: String?

    init {
      require(cfg.contains("name")) { """The "name" field is not defined in the plugin.yml.""" }
      require(cfg.contains("version")) { """The "version" field is not defined in the plugin.yml.""" }
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
