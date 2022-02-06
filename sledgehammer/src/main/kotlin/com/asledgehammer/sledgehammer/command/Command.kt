@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package com.asledgehammer.sledgehammer.command

/**
 * **Command** TODO: Document.
 *
 * @author Jab
 *
 * @property name
 * @property args
 */
class Command(val name: String, val args: List<String>) {

  /** TODO: Document. */
  var raw: String

  init {
    val rawBuilder = StringBuilder("/")
    rawBuilder.append(name)
    for (arg in args) {
      if (arg.contains(" ")) {
        rawBuilder.append(" \"")
        rawBuilder.append(arg.trim { it <= ' ' })
        rawBuilder.append("\"")
      } else {
        rawBuilder.append(" ")
        rawBuilder.append(arg.trim { it <= ' ' })
      }
    }
    raw = rawBuilder.toString()
  }

  companion object {

    /** TODO: Document. */
    @JvmStatic
    fun fromRaw(string: String): Command {
      val command = string
        .replace("/", "")
        .replace("!", "")
        .trim { it <= ' ' }
        .split(" ")
        .toTypedArray()[0]
        .toLowerCase()
      val args = parseArgs(string)
      return Command(command, args)
    }

    private fun parseArgs(input: String): List<String> {
      val chars = input.toCharArray()
      val args = ArrayList<String>()
      var arg = StringBuilder()
      var inside = false
      for (c in chars) {
        when (c) {
          ' ' -> if (!inside) {
            args.add(arg.toString())
            arg = StringBuilder()
          } else {
            arg.append(c)
          }
          '\"' -> inside = !inside
          else -> arg.append(c)
        }
      }
      if (arg.isNotEmpty()) args.add(arg.toString())
      return if (args.size <= 1) ArrayList() else args.subList(1, args.lastIndex)
    }

    /** TODO: Document. */
    @JvmStatic
    fun getSubArgs(args: Array<String?>, index: Int): Array<String?> {
      require(args.isNotEmpty()) { "Arguments Array provided is empty." }
      require(args.size - index >= 0) {
        "index given to start is beyond the last index of the arguments Array provided."
      }
      val ret = arrayOfNulls<String>(args.size - index)
      System.arraycopy(args, index, ret, 0, args.size - index)
      return ret
    }

    /**
     * TODO: Document.
     */
    @JvmOverloads
    fun combineArguments(args: Array<String?>, index: Int, glueString: String? = ""): String {
      require(args.isNotEmpty()) {
        "WARNING: Arguments given is empty for argument combination. Returning as an empty string."
      }
      require(index >= 0) { "Index cannot be a negative value." }
      require(index < args.size) {
        "Index provided is larger or equal to the length of the arguments array given."
      }

      val builder = StringBuilder()
      for (i in index until args.size) {
        if (builder.isEmpty()) builder.append(args[i])
        else builder.append(glueString).append(args[i])
      }
      return builder.toString().trim { it <= ' ' }
    }
  }

  /**
   * **Response** TODO: Document.
   *
   * @author Jab
   */
  class Response {

    /** TODO: Document. */
    var message: String? = null
      private set

    /** TODO: Document. */
    var found = false

    /** TODO: Document. */
    var handled = false

    /** TODO: Document. */
    var denied = false
      private set

    /** TODO: Document. */
    fun deny(message: String) {
      this.message = message
      this.denied = true
      this.handled = true
    }
  }
}
