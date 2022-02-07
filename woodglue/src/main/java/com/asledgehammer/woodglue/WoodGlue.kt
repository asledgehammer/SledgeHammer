package com.asledgehammer.woodglue

import com.asledgehammer.woodglue.util.JarUtils.createJarArchive
import java.io.*
import java.lang.instrument.Instrumentation
import java.nio.file.FileAlreadyExistsException
import java.nio.file.Files
import java.util.jar.JarFile

class WoodGlue(inst: Instrumentation) {

  private lateinit var pzPath: String
  private lateinit var pzDir: File
  private val dirLib = File("lib/")
  private val dirPatches = File("patches/")
  private val dirLibBuilt = File("lib/built/")
  private val dirNatives = File("lib/natives/")
  private val dirPlugins = File("plugins/")

  init {
    createDirs()
    loadSettings()
    copyPZFiles()
    injectJars(inst, dirLib, arrayOf("woodglue"))
    injectPatches(inst, dirPatches, Settings.patches)
    // Do this last so Craftboid classes takes priority.
    injectJars(inst, dirLibBuilt)
  }

  private fun copyPZFiles() {
    packPZCode()
    copyFolder(File(pzDir, "natives"), dirNatives)
    copyFiles(File(pzDir, "java"), dirLib, "jar")
    copyFiles(pzDir, dirNatives, "dll")
    copyMiscFiles()
    handleMediaFolder()
  }

  private fun handleMediaFolder() {
    if (!Settings.copyMediaDir) return
    try {
      val from = File(pzDir, "media")
      val dst = File(from.name)
      if (from.isFile) {
        try {
          Files.copy(from.toPath(), dst.toPath())
          log("Copied ${from.name}.")
        } catch (ignored: FileAlreadyExistsException) {
        }
      } else copyFolder(from, dst)
    } catch (e: IOException) {
      e.printStackTrace()
    }
  }

  private fun copyMiscFiles() {
    var from: File
    var dest: File
    val filesToCopy = arrayOf("${this.pzPath}/steam_appid.txt", "${this.pzPath}/serialize.lua")
    try {
      for (file in filesToCopy) {
        from = File(file)
        dest = File(from.name)
        if (from.isFile) {
          try {
            Files.copy(from.toPath(), dest.toPath())
            log("Copied $file.")
          } catch (ignored: FileAlreadyExistsException) {
          }
        } else copyFolder(from, dest)
      }
    } catch (e: IOException) {
      e.printStackTrace()
    }
  }

  private fun createDirs() {
    if (dirLib.mkdirs()) log("Created: ./${dirLib.path}")
    if (dirPatches.mkdirs()) log("Created: ./${dirPatches.path}")
    if (dirLibBuilt.mkdirs()) log("Created: ./${dirLibBuilt.path}")
    if (dirNatives.mkdirs()) log("Created: ./${dirNatives.path}")
    if (dirPlugins.mkdirs()) log("Created: ./${dirPlugins.path}")
  }

  private fun loadSettings() {
    // Load the Settings for Sledgehammer.
    val settings = Settings

    // Grab the PZ dedicated server directory.
    pzPath = settings.pZServerDirectory!!
    pzPath = pzPath.replace("\\", "/")
    if (pzPath.endsWith("/")) pzPath = pzPath.substring(0, pzPath.length - 1)
    pzDir = File(pzPath)
  }

  private fun packPZCode() {

    if (!npzJar.exists()) {
      val classDir = "$pzPath/java"
      val classDirectories = arrayOf(
        File("$classDir/astar"),
        File("$classDir/com"),
        File("$classDir/de"),
        File("$classDir/fmod"),
        File("$classDir/javax"),
        File("$classDir/N3D"),
        File("$classDir/org"),
        File("$classDir/se"),
        File("$classDir/zombie")
      )

      log("Packing PZ Code: ${npzJar.name}..")
      createJarArchive(npzJar, classDirectories, "zombie.network.GameServer")
    }
  }

  fun injectJars(inst: Instrumentation, dir: File, excludes: Array<String> = arrayOf()) {
    val files = dir.listFiles()
    if (files != null) {
      for (file in files) {
        if (file.name.endsWith("jar")) {

          var found = false
          for (exclude in excludes) {
            if (file.name.contains("WoodGlue")) {
              found = true
              break
            }
          }
          if (found) continue

          try {
            log("Injecting library: ${file.name}..")
            inst.appendToSystemClassLoaderSearch(JarFile(file))
          } catch (e: IOException) {
            e.printStackTrace()
          }
        }
      }
    }
  }

  private fun injectPatches(inst: Instrumentation, dir: File, jars: ArrayList<String>) {
    for (jarName in jars) {
      val jarFile = File(dir, "$jarName.jar")
      try {
        log("Injecting patch: ${jarFile.name}..")
        inst.appendToSystemClassLoaderSearch(JarFile(jarFile))
      } catch (e: IOException) {
        e.printStackTrace()
      }
    }
  }

  companion object {

    const val HEADER = "[WoodGlue] :: "
    val npzJar = File("lib/built/PZ_41.65.jar")

    /**
     * Copies a File source directory to a File destination directory.
     *
     * @param src The File source directory to copy.
     * @param dst The File destination directory to copy to.
     * @throws IOException Thrown with any File Exception.
     */
    @Throws(IOException::class)
    private fun copyFolder(src: File, dst: File) {
      if (src.isDirectory) {
        if (!dst.exists() && dst.mkdirs()) log("Copied  ../${dst.name}")
        val files = src.list()
        if (files != null) {
          for (file in files) copyFolder(File(src, file), File(dst, file))
        }
      } else {
        if (!dst.exists() || dst.length() != src.length()) {
          val inputStream: InputStream = FileInputStream(src)
          val outputStream: OutputStream = FileOutputStream(dst)
          val buffer = ByteArray(1024)
          var length: Int
          while (inputStream.read(buffer).also { length = it } > 0) {
            outputStream.write(buffer, 0, length)
          }
          inputStream.close()
          outputStream.close()
          log("Copied ../${dst.name}")
        }
      }
    }

    /**
     * Copies a File source directory to a File destination directory.
     *
     * @param src The File source directory to copy.
     * @param dst The File destination directory to copy to.
     * @throws IOException Thrown with any File Exception.
     */
    @Throws(IOException::class)
    private fun copyFiles(src: File, dst: File, type: String) {

      require(src.isDirectory && dst.isDirectory)

      val typeLower = type.toLowerCase()

      val files = src.listFiles()
      if (files != null && files.isNotEmpty()) {
        for (file in files) {
          if (file.extension.toLowerCase() == typeLower) {

            val fileDst = File(dst, file.name)

            if (!fileDst.exists() || fileDst.length() != file.length()) {
              val inputStream: InputStream = FileInputStream(file)
              val outputStream: OutputStream = FileOutputStream(fileDst)
              val buffer = ByteArray(1024)
              var length: Int
              while (inputStream.read(buffer).also { length = it } > 0) {
                outputStream.write(buffer, 0, length)
              }
              inputStream.close()
              outputStream.close()
              log("Copied ../${fileDst.name}")
            }
          }
        }
      }
    }

    @JvmStatic
    fun log(line: String) {
      println("$HEADER$line")
    }
  }
}
