package com.asledgehammer.woodglue

import com.asledgehammer.woodglue.util.CreateJarFile.createJarArchive
import java.io.*
import java.lang.instrument.Instrumentation
import java.nio.file.FileAlreadyExistsException
import java.nio.file.Files
import java.util.jar.JarFile

const val HEADER = "[WoodGlue] :: "

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
    if (!dst.exists() && dst.mkdirs()) WoodGlue.log("Copied  ../${dst.name}")
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
      WoodGlue.log("Copied ../${dst.name}")
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
          WoodGlue.log("Copied ../${fileDst.name}")
        }
      }
    }
  }
}

class WoodGlue(instrumentation: Instrumentation) {

  init {
    // Make sure that the Sledgehammer folders exist.
    if (File("bin/natives/").mkdirs()) println("$HEADER Created directory: .../natives/")
    if (File("plugins/").mkdirs()) println("$HEADER Created directory: .../plugins/")

    val npzJar = File("bin/PZ-41.65.jar")

    // Load the Settings for Sledgehammer.
    val settings = Settings

    // Grab the PZ dedicated server directory.
    var pzDirectory = settings.pZServerDirectory
    pzDirectory = pzDirectory!!.replace("\\", "/")
    if (pzDirectory.endsWith("/")) pzDirectory = pzDirectory.substring(0, pzDirectory.length - 1)

    val pzDir = File(Settings.pZServerDirectory!!)

    // Tell the console the registered PZ directory.
    log("PZDirectory: '$pzDirectory'")

    val classDir = "$pzDirectory/java"

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

    if (!npzJar.exists()) {
      createJarArchive(npzJar, classDirectories, "zombie.network.GameServer")
    }

    copyFolder(File(pzDir, "natives"), File("bin/natives"))
    copyFiles(File(pzDir, "java"), File("bin/"), "jar")
    copyFiles(pzDir, File("bin/natives/"), "dll")

    val fileNativeDirectory = File("bin/")
    val files = fileNativeDirectory.listFiles()
    if (files != null) {
      for (file in files) {
        if (file.name.endsWith("jar")) {

          if (file.name.equals(npzJar.name) || file.name.contains("WoodGlue")) {
            continue
          }

          try {
            log("Loading library: ${file.name}")
            instrumentation.appendToSystemClassLoaderSearch(JarFile(file))
          } catch (e: IOException) {
            e.printStackTrace()
          }
        }
      }
    }

    var from: File
    var dest: File
    val filesToCopy = arrayOf("$pzDirectory/steam_appid.txt", "$pzDirectory/media")
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

    val mainFiles = arrayOf(File("$pzDirectory/serialize.lua"))
    for (file in mainFiles) {
      val dst = File(file.name)
      try {
        Files.copy(file.toPath(), dst.toPath())
      } catch (ignored: FileAlreadyExistsException) {
      } catch (e: IOException) {
        e.printStackTrace()
      }
    }

    // Load this last so that CraftBoid can take priority in the System
    //   ClassLoader, overriding the NPZ code.
    try {
      instrumentation.appendToSystemClassLoaderSearch(JarFile(npzJar))
    } catch (e: IOException) {
      e.printStackTrace()
    }
  }

  companion object {
    @JvmStatic
    fun log(line: String) {
      println("$HEADER$line")
    }
  }
}
