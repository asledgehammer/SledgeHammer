package com.asledgehammer.woodglue.util

import com.asledgehammer.woodglue.WoodGlue
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.*
import java.util.jar.Attributes
import java.util.jar.JarEntry
import java.util.jar.JarOutputStream
import java.util.jar.Manifest
import java.util.zip.ZipException

/**
 * TODO: Document.
 *
 * @author Jab
 */
object CreateJarFile {

    var BUFFER_SIZE = 10240

    fun createJarArchive(archiveFile: File, directories: Array<File>, mainClass: String? = null) {
        try {
            val manifest = Manifest()
            if(mainClass != null) {
                manifest.mainAttributes[Attributes.Name.MAIN_CLASS] = mainClass
            }

            val buffer = ByteArray(BUFFER_SIZE)
            // Open archive file
            val stream = FileOutputStream(archiveFile)
            val out = JarOutputStream(stream, manifest)
            for (directory in directories) {
                val files = getFiles(directory, ".class")
                val split = directory.name
                for (file in files) {
                    val path = getJarPath(file, split)
                    WoodGlue.log("Copied ${file.name}")
                    // Add archive entry
                    val jarAdd = JarEntry("$split/$path")
                    jarAdd.time = file.lastModified()
                    try {
                        out.putNextEntry(jarAdd)
                    } catch (e: ZipException) {
                        continue
                    }
                    // Write file to archive
                    val inputStream = FileInputStream(file)
                    while (true) {
                        val nRead = inputStream.read(buffer, 0, buffer.size)
                        if (nRead <= 0) break
                        out.write(buffer, 0, nRead)
                    }
                    inputStream.close()
                }
            }
            out.close()
            stream.close()
        } catch (e: Exception) {
            e.printStackTrace(System.err)
        }
    }

    private fun getJarPath(file: File, directory: String): String {
        val ext = file.absolutePath
        val split = ext.replace("\\", "/").split("/").toTypedArray()
        var positionBegin = 0
        for (index in split.indices) {
            if (split[index].equals(directory, ignoreCase = true)) {
                positionBegin = index + 1
                break
            }
        }
        val classPath = StringBuilder()
        for (index in positionBegin until split.size) {
            classPath.append(split[index])
            classPath.append("/")
        }
        return classPath.toString().substring(0, classPath.length - 1)
    }

    private fun getFiles(directory: File, extension: String): List<File> {
        val listFiles = ArrayList<File>()
        if (directory.exists()) {
            val files = directory.listFiles()
            if (files != null) {
                for (file in files) {
                    if (file.isDirectory) {

                        // Recursive cannot iterate over parent directories.
                        if (file.name == "." || file.name == ".." || file.name == "...") continue

                        // Not a part of PZ.
                        if (file.name.equals("rcon", ignoreCase = true)) continue

                        val newFiles = getFiles(file, extension)
                        if (newFiles.isNotEmpty()) listFiles.addAll(newFiles)
                    } else {
                        if (file.name.endsWith(extension)) listFiles.add(file)
                    }
                }
            }
        }
        return listFiles
    }
}
