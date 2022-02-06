@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package com.asledgehammer.framework.cfg

import java.io.File

/**
 * **ConfigFile** wraps ConfigSection, parsing and writing YAML data.
 *
 * @author Jab
 */
open class CFGFile(var file: File?) : CFGSection("")
