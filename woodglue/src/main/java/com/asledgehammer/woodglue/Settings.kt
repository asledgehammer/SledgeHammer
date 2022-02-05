@file:Suppress("SameParameterValue")

package com.asledgehammer.woodglue

import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.error.YAMLException
import java.io.*
import java.net.URL
import java.util.*

/**
 * Class to load and manage the settings for the Sledgehammer engine.
 *
 * @author Jab
 */
object Settings {

  private val NEW_LINE = System.getProperty("line.separator")
  private const val fileName = "woodglue.yml"
  private val fileConfig: File = File(fileName)

  private lateinit var map: Map<*, *>

  var pZServerDirectory: String? = null
    private set

  var copyMediaDir: Boolean = true
    private set

  val patches = ArrayList<String>()

  /**
   * @return Returns the global instance of Yaml.
   */
  private val yaml: Yaml

  init {

    val options = DumperOptions()
    options.defaultFlowStyle = DumperOptions.FlowStyle.BLOCK
    options.defaultScalarStyle = DumperOptions.ScalarStyle.LITERAL
    yaml = Yaml(options)

    if (!fileConfig.exists()) {
      saveDefaultConfig()
      try {
        setDir(requestDir(), true)
        setCopyMediaDir(requestCopyMediaDir(), true)
      } catch (e: Exception) {
        e.printStackTrace()
      }
    }
    loadConfig()
  }

  /**
   * Reads and interprets the YAML from the woodglue.yml File.
   */
  private fun loadConfig() {
    try {
      val fis = FileInputStream(fileConfig)
      map = yaml.load(fis) as Map<*, *>
      fis.close()
      parseConfig()
    } catch (e: YAMLException) {
      System.err.println("Failed to parse YAML.")
      e.printStackTrace()
    } catch (e: FileNotFoundException) {
      System.err.println("Config file does not exist.")
    } catch (e: IOException) {
      System.err.println("Failed to read $fileName")
    }
  }

  /**
   * Parses and interprets the YAML setting sections.
   */
  private fun parseConfig() {
    val oPZServerDirectory = map["pz_server_dir"]
    if (oPZServerDirectory != null) {
      val s = oPZServerDirectory.toString()
      if (s.isNotEmpty()) setDir(s, false)
      else setDir(requestDir(), true)
    } else setDir(requestDir(), true)

    val oCopyMediaFolder = map["copy_media_dir"]
    if (oCopyMediaFolder != null) {
      this.copyMediaDir = oCopyMediaFolder as Boolean
    } else {
      setCopyMediaDir(requestCopyMediaDir(), true)
    }

    val oPatches =
      map["patches"] as List<*>? ?: throw YAMLException("The list 'patches' is not defined in 'woodglue.yml")

    for (o in oPatches) {
      patches.add(o.toString())
    }
  }

  /**
   * Sets the directory path to the vanilla distribution of the Project Zomboid Dedicated Server.
   *
   * @param pzServerDirectory The directory path to set.
   * @param save Flag to save the setting.
   */
  private fun setDir(pzServerDirectory: String, save: Boolean) {

    pZServerDirectory = pzServerDirectory

    if (save) {
      val lines = readConfigFile()
      for (index in lines.indices) {
        val line = lines[index]
        val interpreted = line.trim { it <= ' ' }
        if (interpreted.startsWith("#")) continue
        val spaces = getLeadingSpaceCount(line)
        if (interpreted.startsWith("pz_server_dir:")) {
          val newLine = getSpaces(spaces) + "pz_server_dir: '" + pzServerDirectory + "'"
          lines[index] = newLine
          break
        }
      }
      writeConfigFile(lines)
    }
  }

  private fun setCopyMediaDir(flag: Boolean, save: Boolean) {

    copyMediaDir = flag

    if (save) {
      val lines = readConfigFile()
      for (index in lines.indices) {
        val line = lines[index]
        val interpreted = line.trim { it <= ' ' }
        if (interpreted.startsWith("#")) continue
        val spaces = getLeadingSpaceCount(line)
        if (interpreted.startsWith("copy_media_dir:")) {
          val newLine = getSpaces(spaces) + "copy_media_dir: $flag"
          lines[index] = newLine
          break
        }
      }
      writeConfigFile(lines)
    }
  }

  /**
   * Reads the config.yml file as a List of lines.
   *
   * @return Returns a List of lines.
   */
  private fun readConfigFile(): ArrayList<String> {
    val lines = ArrayList<String>()
    try {
      val fr = FileReader(fileName)
      val br = BufferedReader(fr)

      var line: String?
      do {
        line = br.readLine()
        if (line != null) lines.add(line)
      } while (line != null)

      br.close()
      fr.close()
    } catch (e: IOException) {
      e.printStackTrace()
    }
    return lines
  }

  /**
   * Writes a Jar File Entry to the given destination File.
   *
   * @param jar The File Object of the Jar File.
   * @param source The source path inside the Jar File to the Entry.
   * @param destination The File destination to write the Jar File Entry.
   */
  private fun write(jar: File, source: String, destination: File) {
    try {
      val inputStream = getStream(jar, source)
      val os: OutputStream = FileOutputStream(destination)
      val buffer = ByteArray(102400)
      var bytesRead: Int
      while (inputStream.read(buffer).also { bytesRead = it } != -1) {
        os.write(buffer, 0, bytesRead)
      }
      inputStream.close()
      os.flush()
      os.close()
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }

  private fun requestDir(): String {
    var pzDirectory: String? = null
    var input: String
    val scanner = Scanner(System.`in`)
    while (pzDirectory == null) {
      WoodGlue.log("Please enter the directory for the Project Zomboid Dedicated Server:")
      input = scanner.nextLine()
      input = input.replace("\\", "/")
      if (!input.endsWith("/")) input += "/"
      val directory = File(input)
      if (directory.exists() && directory.isDirectory) {
        val zombieDirectory = File(input + File.separator + "java" + File.separator + "zombie")
        if (zombieDirectory.exists() && zombieDirectory.isDirectory) {
          pzDirectory = input
        } else {
          WoodGlue.log("This is a directory, but it does not contain Project Zomboid files.")
        }
      } else {
        WoodGlue.log("Not a valid directory.")
      }
    }
    return pzDirectory.replace("\\", "/")
  }

  private fun requestCopyMediaDir(): Boolean {
    var i: String
    val scanner = Scanner(System.`in`)
    var valid = false
    while (!valid) {
      WoodGlue.log("Copy media directory? (y)")
      i = scanner.nextLine().toLowerCase()
      if (i.isEmpty()) i = "y"
      if (i == "y" || i == "n" || i == "yes" || i == "no") {
        copyMediaDir = i == "y" || i == "yes"
        valid = true
      }
    }
    return copyMediaDir
  }

  /**
   * Writes the config.yml File with a List of lines.
   *
   * @param lines The List of lines to save.
   */
  private fun writeConfigFile(lines: List<String>) {
    try {
      val fw = FileWriter(fileName)
      val bw = BufferedWriter(fw)
      for (line in lines) bw.write(line + NEW_LINE)
      bw.close()
      fw.close()
    } catch (e: IOException) {
      System.err.println("Failed to save $fileName")
      e.printStackTrace()
    }
  }

  /**
   * @param string The String being interpreted.
   * @return Returns the count of spaces in front of any text in the given line.
   */
  private fun getLeadingSpaceCount(string: String?): Int {
    requireNotNull(string) { "String given is null." }
    if (string.isEmpty()) return 0
    var spaces = 0
    val chars = string.toCharArray()
    for (c in chars) {
      if (c != ' ') break
      spaces++
    }
    return spaces
  }

  /**
   * @param length The count of spaces to add to the returned String.
   * @return Returns a valid YAML character sequence of spaces as a String.
   */
  private fun getSpaces(length: Int): String {
    require(length >= 0) { "length given is less than 0." }
    val string = StringBuilder()
    for (index in 0 until length) string.append(" ")
    return string.toString()
  }

  /**
   * Saves the template of the config.yml in the Sledgehammer.jar to the server folder.
   */
  private fun saveDefaultConfig() {
    val file = File(Settings::class.java.protectionDomain.codeSource.location.toURI().path)
    write(file, fileName, File(fileName))
  }

  /**
   * @param jar The File Object of the Jar File.
   * @param source The source path inside the Jar File.
   * @return Returns an InputStream of the Jar File Entry.
   * @throws IOException Thrown with File Exceptions.
   */
  @Throws(IOException::class)
  private fun getStream(jar: File, source: String): InputStream {
    return URL("jar:file:" + jar.absolutePath + "!/" + source).openStream()
  }
}
