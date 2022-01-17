package com.asledgehammer.woodglue

import jab.sledgehammer.util.CreateJarFile.createJarArchive
import java.io.*
import java.lang.instrument.Instrumentation
import java.nio.file.FileAlreadyExistsException
import java.nio.file.Files
import java.util.jar.JarFile

class WoodGlue(args: String, instrumentation: Instrumentation) {

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
            if (!dst.exists() && dst.mkdirs()) println("$HEADER Copied  ../${dst.name}")
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
                println("$HEADER Copied ../${dst.name}")
            }
        }
    }

    init {
        // Make sure that the Sledgehammer folders exist.


        // Make sure that the Sledgehammer folders exist.
        if (File("natives/").mkdirs()  ) println("$HEADER Created directory: .../natives/"  )
        if (File("plugins/").mkdirs()  ) println("$HEADER Created directory: .../plugins/"  )
        if (File("settings/").mkdirs() ) println("$HEADER Created directory: .../settings/" )
        if (File("lang/").mkdirs()     ) println("$HEADER Created directory: .../lang/"     )
        if (File("lua/").mkdirs()      ) println("$HEADER Created directory: .../lua/"      )
        if (File("cache/").mkdirs()    ) println("$HEADER Created directory: .../cache/"    )
        if (File("logs/").mkdirs()     ) println("$HEADER Created directory: .../logs/"     )
        if (File("cache/map/").mkdirs()) println("$HEADER Created directory: .../cache/map/")

        val craftboid = File("natives/CraftBoid.jar")

        // Load the Settings for Sledgehammer.
        val settings = Settings

        // Grab the PZ dedicated server directory.
        var pzDirectory = settings.pZServerDirectory
        pzDirectory = pzDirectory!!.replace("\\", "/")
        if (pzDirectory.endsWith("/")) pzDirectory = pzDirectory.substring(0, pzDirectory.length - 1)

        // Tell the console the registered PZ directory.
        println("$HEADER PZDirectory: \"$pzDirectory\"")

        val classDir = "$pzDirectory/java"
        val nativeDir = "$pzDirectory/natives"

        val classDirectories = arrayOf(
            File("$classDir/astar"),
            File("$classDir/com"),
            File("$classDir/de"),
            File("$classDir/fmod"),
            File("$classDir/javax"),
            File("$classDir/org"),
            File("$classDir/se"),
            File("$classDir/zombie"))

        val additionalFiles = arrayOf(
            File("$nativeDir/celt_encoder.dll"),
            File("$nativeDir/fmod.dll"),
            File("$nativeDir/fmod64.dll"),
            File("$nativeDir/fmodintegration32.dll"),
            File("$nativeDir/fmodintegration64.dll"),
            File("$nativeDir/fmodstudio.dll"),
            File("$nativeDir/fmodstudio64.dll"),
            File("$nativeDir/jinput-dx8.dll"),
            File("$nativeDir/jinput-dx8_64.dll"),
            File("$nativeDir/jinput-raw.dll"),
            File("$nativeDir/jinput-raw_64.dll"),
            File("$nativeDir/Lighting32.dll"),
            File("$nativeDir/Lighting64.dll"),
            File("$nativeDir/lwjgl.dll"),
            File("$nativeDir/lwjgl64.dll"),
            File("$nativeDir/msvcp100.dll"),
            File("$nativeDir/msvcp120.dll"),
            File("$nativeDir/msvcr100.dll"),
            File("$nativeDir/OpenAL32.dll"),
            File("$nativeDir/OpenAL64.dll"),
            File("$nativeDir/OpenALSoft32.dll"),
            File("$nativeDir/PZ_XInput32.dll"),
            File("$nativeDir/PZ_XInput64.dll"),
            File("$nativeDir/PZBullet32.dll"),
            File("$nativeDir/PZBullet32d.dll"),
            File("$nativeDir/PZBullet64.dll"),
            File("$nativeDir/PZBullet64d.dll"),
            File("$nativeDir/PZPopMan32.dll"),
            File("$nativeDir/PZPopMan64.dll"),
            File("$nativeDir/RakNet32.dll"),
            File("$nativeDir/RakNet64.dll"),
            File("$nativeDir/steam_api.dll"),
            File("$nativeDir/steam_api64.dll"),
            File("$nativeDir/steamintegration64.dll"),
            File("$nativeDir/steamworks4j.dll"),
            File("$nativeDir/steamworks4j64.dll"),
            File("$nativeDir/ZNetJNI32.dll"),
            File("$nativeDir/ZNetJNI64.dll"),
            File("$nativeDir/ZNetNoSteam32.dll"),
            File("$nativeDir/ZNetNoSteam64.dll"),  // JARS
            File("$classDir/guava-23.0.jar"),
            File("$classDir/javacord-2.0.17-shaded.jar"),
            File("$classDir/jinput.jar"),
            File("$classDir/junit-4.12.jar"),
            File("$classDir/lwjgl.jar"),
            File("$classDir/lwjgl_test.jar"),
            File("$classDir/lwjgl_util.jar"),
            File("$classDir/lwjgl_util_applet.jar"),
            File("$classDir/sqlite-jdbc-3.8.10.1.jar"),
            File("$classDir/sqlitejdbc-v056.jar"),
            File("$classDir/trove-3.0.3.jar"),
            File("$classDir/uncommons-maths-1.2.3.jar")
        )

        if (!craftboid.exists()) {
            createJarArchive(craftboid, classDirectories, arrayOf())
        }

        try {
            instrumentation.appendToSystemClassLoaderSearch(JarFile(craftboid))
        } catch (e: IOException) {
            e.printStackTrace()
        }

        val mainFiles = arrayOf(
            File("$pzDirectory/stdlib.lbc"),
            File("$pzDirectory/stdlib.lua"),
            File("$pzDirectory/serialize.lua"))

        for (file in mainFiles) {
            val dest = File(file.name)
            try {
                Files.copy(file.toPath(), dest.toPath())
            } catch (ignored: FileAlreadyExistsException) {
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        for (file in additionalFiles) {
            val dest = File("natives/" + file.name)
            try {
                Files.copy(file.toPath(), dest.toPath())
            } catch (ignored: FileAlreadyExistsException) {
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        val fileNativeDirectory = File("natives/")
        val files = fileNativeDirectory.listFiles();
        if (files != null) {
            for (file in files) {
                if (file.name.endsWith("jar")) {
                    try {
                        println("$HEADER Loading library: ${file.name}")
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
                        println("$HEADER Copied $file...")
                    } catch (ignored: FileAlreadyExistsException) {
                    }
                } else copyFolder(from, dest)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    companion object {
        val HEADER = "[WoodGlue] ::"
    }
}
