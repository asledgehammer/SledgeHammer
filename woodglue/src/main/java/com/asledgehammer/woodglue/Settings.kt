package com.asledgehammer.woodglue

import com.asledgehammer.sledgehammer.Sledgehammer
import jab.sledgehammer.util.YamlUtil
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
    /** The raw YAML Map data for the config.yml File.  */
    private lateinit var map: Map<*, *>

    /**
     * The String names of the Player accounts to exempt from the check to remove inactive accounts.
     */
    private var listAccountsExcluded: MutableList<String>? = null

    /** The File Object of the config.yml File.  */
    private val fileConfig: File
    /**
     * @return Returns the directory path to the vanilla Project Zomboid Dedicated Server
     * installation.
     */
    /**
     * The set directory to the vanilla distribution installation of the ProjectZomboid Dedicated
     * Server.
     */
    var pZServerDirectory: String? = null
        private set
    /** @return Returns the String message when a permission is denied.
     */
    /** The set String message when a permission is denied.  */
    var permissionDeniedMessage: String? = null
        private set
    /** @return Returns the String password for the Administrator account.
     */
    /** The String password for the Administrator account.  */
    var administratorPassword: String? = null
        private set
    /** @return Returns the URL that points to the MongoDB service.
     */
    /** The String URL pointing at the MongoDB storing data for Sledgehammer.  */
    var databaseURL: String? = null
        private set
    /** @return Returns the PORT that the MongoDB service listens on.
     */
    /** The String PORT the MongoDB server listens on.  */
    var databasePort = 0
        private set
    /** @return Returns the user-name for the Sledgehammer MongoDB account.
     */
    /** The String username of the Sledgehammer account for MongoDB.  */
    var databaseUsername: String? = null
        private set
    /** @return Returns the password for the Sledgehammer MongoDB account.
     */
    /** The String password of the Sledgehammer account for MongoDB.  */
    var databasePassword: String? = null
        private set
    /** @return Returns the database in the MongoDB server that defines the Sledgehammer account.
     */
    /** The String database the Sledgehammer account is defined in.  */
    var databaseDatabase: String? = null
        private set
    /** @return Returns the time in days for inactive accounts to be removed.
     */
    /**
     * The Integer amount of days an account has until it is considered expired. Set to 0 to disable
     * the utility.
     */
    var accountIdleExpireTime = 0
        private set
    /**
     * @return Returns the maximum radius an explosion is allowed for a server. Anything above this
     * limit is registered is cancelled, and invokes a CheaterEvent.
     */
    /**
     * (Private Method)
     *
     *
     * Sets the maximum radius an explosion is allowed for a server. Anything above this limit is
     * registered is cancelled, and invokes a CheaterEvent.
     *
     * @param explosionRadiusMaximum The radius to set.
     */
    /**
     * The maximum Integer radius an explosion is allowed for a server. Anything above this limit is
     * registered is cancelled, and invokes a CheaterEvent.
     */
    var maximumExplosionRadius = 0
        private set
    /** @return Returns true if Sledgehammer is set in debug-mode.
     */
    /**
     * (Private Method)
     *
     *
     * Sets the debug flag for Sledgehammer.
     *
     * @param debug The Boolean flag to set.
     */
    /** The Debug flag for the Sledgehammer engine.  */
    var isDebug = false
        private set(debug) {
            field = debug
            Sledgehammer.debug = debug
        }

    /** Flag to enable the native RCON utility for the PZ server.  */
    private var allowRCON = false

    /** Flag to enable Helicopter events on the PZ server.  */
    private var allowHelicopters = false
    private var overrideLua = false
    private var overrideLang = false
    val name: String
        get() = "Sledgehammer->config.yml"

    init {
        fileConfig = File("config.yml")
        if (!fileConfig.exists()) {
            saveDefaultConfig()
            try {
//                generateAdministratorPassword()
                setPZServerDirectory(requestPZDedicatedServerDirectory(), true)
                setDatabaseURL(requestDatabaseURL(), true)
                setDatabasePort(requestDatabasePORT(), true)
                setDatabaseUsername(requestDatabaseUsername(), true)
                setDatabasePassword(requestDatabasePassword(), true)
                setDatabaseDatabase(requestDatabaseDatabase(), true)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        loadConfig()
    }

    /**
     * (Private Method)
     *
     *
     * Reads and interprets the YAML from the config.yml File.
     */
    private fun loadConfig() {
        try {
            val fis = FileInputStream(fileConfig)
            map = YamlUtil.yaml.load(fis) as Map<*, *>
            fis.close()
            parseConfig()
        } catch (e: YAMLException) {
            System.err.println("Failed to parse YAML.")
            e.printStackTrace()
        } catch (e: FileNotFoundException) {
            System.err.println("Config file does not exist.")
        } catch (e: IOException) {
            System.err.println("Failed to read config.yml")
        }
    }

    /**
     * (Private Method)
     *
     *
     * Parses and interprets the YAML setting sections.
     */
    private fun parseConfig() {
        parseGeneralConfig(map["general"] as Map<*, *>?)
        parseSecurityConfig(map["security"] as Map<*, *>?)
        parseDatabaseConfig(map["mongo_db"] as Map<*, *>?)
    }

    /**
     * (Private Method)
     *
     *
     * Parses and interprets the general section.
     *
     * @param general The Map definition.
     */
    private fun parseGeneralConfig(general: Map<*, *>?) {
        if (general == null) {
            return
        }
        // (Boolean) general.debug
        val oDebug = general["debug"]
        if (oDebug != null) {
            isDebug = getBoolean(oDebug)
        }
        // (String) general.pz_server_directory
        val oPZServerDirectory = general["pz_server_directory"]
        if (oPZServerDirectory != null) {
            val s = oPZServerDirectory.toString()
            if (!s.isEmpty()) {
                setPZServerDirectory(s, false)
            } else {
                requestPZDedicatedServerDirectory()
            }
        } else {
            requestPZDedicatedServerDirectory()
        }
        // (String) general.permission_message_denied
        val oPermissionMessageDenied = general["permission_message_denied"]
        if (oPermissionMessageDenied != null) {
            setPermissionMessageDenied(oPermissionMessageDenied.toString(), false)
        }
        // (String) general.account_idle_expire_time
        val oAccountIdleExpireTime = general["account_idle_expire_time"]
        if (oAccountIdleExpireTime != null) {
            val s = oAccountIdleExpireTime.toString()
            try {
                val value = s.toInt()
                if (value < 0) {
                    System.err.println("account_idle_expire_time not valid: $s")
                    System.err.println("Number is supposed to be a non-zero, non-negative integer.")
                    System.err.println("Setting value to 0. (disabled)")
                    setAccountIdleExpireTime(0, false)
                } else {
                    setAccountIdleExpireTime(value, false)
                }
            } catch (e: NumberFormatException) {
                System.err.println("account_idle_expire_time not valid: $s")
                System.err.println("Number is supposed to be a non-zero, non-negative integer.")
                System.err.println("Setting value to 0. (disabled)")
                setAccountIdleExpireTime(0, false)
            }
        }
        listAccountsExcluded = LinkedList<String>()
        // (List) genera.account_idle_exclusions
        val oAccountIdleExclusions = general["account_idle_exclusions"]
        if (oAccountIdleExclusions != null && oAccountIdleExclusions is List<*>) {
            for (o in oAccountIdleExclusions) {
                listAccountsExcluded!!.add(o.toString())
            }
        }
        // (Boolean) general.allow_helicopters
        val oAllowHelicopters = general["allow_helicopters"]
        if (oAllowHelicopters != null) {
            setAllowHelicopters(getBoolean(oAllowHelicopters.toString()), false)
        } else {
            setAllowHelicopters(true, false)
        }
        // (Boolean) general.overrideLua
        val oOverrideLua = general["override_lua"]
        if (oOverrideLua != null) {
            setOverrideLua(getBoolean(oOverrideLua.toString()), false)
        } else {
            setOverrideLua(overrideLua = false, save = false)
        }
        // (Boolean) general.overrideLang
        val oOverrideLang = general["override_lang"]
        if (oOverrideLang != null) {
            setOverrideLang(getBoolean(oOverrideLang.toString()), false)
        } else {
            setOverrideLang(overrideLang = false, save = false)
        }
    }

    /**
     * (Private Method)
     *
     *
     * Parses and interprets the security section.
     *
     * @param security The Map definition.
     */
    private fun parseSecurityConfig(security: Map<*, *>?) {
        // (String) security.administrator_password
        val oAdministratorPassword = security!!["administrator_password"]
        if (oAdministratorPassword != null) {
            val s = oAdministratorPassword.toString()
            if (!s.isEmpty()) {
                setAdministratorPassword(s, false)
            } else {
//                generateAdministratorPassword()
            }
        } else {
//            generateAdministratorPassword()
        }
        // (Integer) security.maximum_explosion_radius
        val oMaximumExplosionRadius = security["maximum_explosion_radius"]
        if (oMaximumExplosionRadius != null) {
            try {
                val value = oMaximumExplosionRadius.toString().toInt()
                if (value <= 0) {
                    maximumExplosionRadius = 12
                    System.err.println("Number not valid: $oMaximumExplosionRadius")
                    System.err.println("Number is supposed to be a non-zero, non-negative integer.")
                    System.err.println("Setting value to 12.")
                } else {
                    maximumExplosionRadius = value
                }
            } catch (e: NumberFormatException) {
                System.err.println("Failed to set security.maximum_explosion_radius")
                System.err.println("Number not valid: $oMaximumExplosionRadius")
                System.err.println("Number is supposed to be a non-zero, non-negative integer.")
                System.err.println("Setting value to 12.")
            }
        }
        // (Boolean) security.allow_rcon
        val oAllowRCON = security["allow_rcon"]
        if (oAllowRCON != null) {
            setAllowRCON(getBoolean(oAllowRCON))
        }
    }

    /**
     * (Private Method)
     *
     *
     * Parses and interprets the MongoDB database section.
     *
     * @param mongoDB The Map definition.
     */
    private fun parseDatabaseConfig(mongoDB: Map<*, *>?) {
        // (String) database.url
        val oDatabaseURL = mongoDB!!["url"]
        if (oDatabaseURL != null) {
            val url = oDatabaseURL.toString()
            if (!url.isEmpty()) {
                setDatabaseURL(oDatabaseURL.toString(), false)
            } else {
                setDatabaseURL(requestDatabaseURL(), true)
            }
        }
        // (Short) database.port
        val oDatabasePort = mongoDB["port"]
        if (oDatabasePort != null) {
            try {
                val value = oDatabasePort.toString().toShort()
                if (value.toInt() == 0) {
                    System.err.println("Failed to set database.port")
                    System.err.println("Number not valid: $oDatabasePort")
                    System.err.println("Number is supposed to be a non-zero, non-negative signed short (1-32767).")
                    System.err.println("Setting value to 27017.")
                    setDatabasePort(27017, true)
                } else {
                    setDatabasePort(value.toInt(), false)
                }
            } catch (e: NumberFormatException) {
                System.err.println("Failed to set database.port")
                System.err.println("Number not valid: $oDatabasePort")
                System.err.println("Number is supposed to be a non-zero, non-negative signed short (1-32767).")
                System.err.println("Setting value to 27017.")
                setDatabasePort(27017, true)
            }
        }
        // (String) database.username
        val oDatabaseUsername = mongoDB["username"]
        if (oDatabaseUsername != null) {
            val username = oDatabaseUsername.toString()
            if (!username.isEmpty()) {
                setDatabaseUsername(username, false)
            } else {
                setDatabaseUsername(requestDatabaseUsername(), true)
            }
        }
        // (String) database.password
        val oDatabasePassword = mongoDB["password"]
        if (oDatabasePassword != null) {
            val password = oDatabasePassword.toString()
            if (!password.isEmpty()) {
                setDatabasePassword(password, false)
            } else {
                setDatabasePassword(requestDatabasePassword(), true)
            }
        }
        // (String) database.database
        val oDatabaseDatabase = mongoDB["database"]
        if (oDatabaseDatabase != null) {
            val database = oDatabaseDatabase.toString()
            if (!database.isEmpty()) {
                setDatabaseDatabase(oDatabaseDatabase.toString(), false)
            } else {
                setDatabaseDatabase(requestDatabaseDatabase(), true)
            }
        }
    }

    /**
     * @param overrideLua The flag to set.
     * @param save The flag to save the Settings.
     */
    fun setOverrideLua(overrideLua: Boolean, save: Boolean) {
        Settings.overrideLua = overrideLua
        if (save) {
            val lines = readConfigFile()
            if (lines != null) {
                for (index in lines.indices) {
                    val line = lines[index]
                    val interpreted = line.trim { it <= ' ' }
                    if (interpreted.startsWith("#")) {
                        continue
                    }
                    val spaces = getLeadingSpaceCount(line)
                    if (interpreted.startsWith("override_lua:")) {
                        val newLine = getSpaces(spaces) + "override_lua: \"" + overrideLua + "\""
                        lines[index] = newLine
                    }
                }
                writeConfigFile(lines)
            }
        }
    }

    /**
     * @param overrideLang The flag to set.
     * @param save The flag to save the Settings.
     */
    fun setOverrideLang(overrideLang: Boolean, save: Boolean) {
        Settings.overrideLang = overrideLang
        if (save) {
            val lines = readConfigFile()
            if (lines != null) {
                for (index in lines.indices) {
                    val line = lines[index]
                    val interpreted = line.trim { it <= ' ' }
                    if (interpreted.startsWith("#")) {
                        continue
                    }
                    val spaces = getLeadingSpaceCount(line)
                    if (interpreted.startsWith("override_lang:")) {
                        val newLine = getSpaces(spaces) + "override_lang: \"" + overrideLang + "\""
                        lines[index] = newLine
                    }
                }
                writeConfigFile(lines)
            }
        }
    }

    /**
     * Sets the flag for allowing Helicopters.
     *
     * @param save Flag to save the setting.
     */
    fun setAllowHelicopters(allowHelicopters: Boolean, save: Boolean) {
        Settings.allowHelicopters = allowHelicopters
        if (save) {
            val lines = readConfigFile()
            if (lines != null) {
                for (index in lines.indices) {
                    val line = lines[index]
                    val interpreted = line.trim { it <= ' ' }
                    if (interpreted.startsWith("#")) {
                        continue
                    }
                    val spaces = getLeadingSpaceCount(line)
                    if (interpreted.startsWith("allow_helicopters:")) {
                        val newLine = getSpaces(spaces) + "allow_helicopters: \"" + allowHelicopters + "\""
                        lines[index] = newLine
                    }
                }
                writeConfigFile(lines)
            }
        }
    }

    /**
     * Sets the message to send when a permission request is denied.
     *
     * @param permissionMessageDenied The message to set.
     * @param save Flag to save the setting.
     */
    fun setPermissionMessageDenied(permissionMessageDenied: String, save: Boolean) {
        permissionDeniedMessage = permissionMessageDenied
        if (save) {
            val lines = readConfigFile()
            if (lines != null) {
                for (index in lines.indices) {
                    val line = lines[index]
                    val interpreted = line.trim { it <= ' ' }
                    if (interpreted.startsWith("#")) {
                        continue
                    }
                    val spaces = getLeadingSpaceCount(line)
                    if (interpreted.startsWith("permission_message_denied:")) {
                        val newLine = (getSpaces(spaces)
                                + "permission_message_denied: \""
                                + permissionMessageDenied
                                + "\"")
                        lines[index] = newLine
                    }
                }
                writeConfigFile(lines)
            }
        }
    }

    /**
     * Sets the directory path to the vanilla distribution of the Project Zomboid Dedicated Server.
     *
     * @param pzServerDirectory The directory path to set.
     * @param save Flag to save the setting.
     */
    fun setPZServerDirectory(pzServerDirectory: String, save: Boolean) {
        pZServerDirectory = pzServerDirectory
        if (save) {
            val lines = readConfigFile()
            if (lines != null) {
                for (index in lines.indices) {
                    val line = lines[index]
                    val interpreted = line.trim { it <= ' ' }
                    if (interpreted.startsWith("#")) {
                        continue
                    }
                    val spaces = getLeadingSpaceCount(line)
                    if (interpreted.startsWith("pz_server_directory:")) {
                        val newLine = getSpaces(spaces) + "pz_server_directory: \"" + pzServerDirectory + "\""
                        lines[index] = newLine
                    }
                }
                writeConfigFile(lines)
            }
        }
    }

    /**
     * Sets the password of the Administrator Player account for the Sledgehammer engine.
     *
     * @param administratorPassword The password to set.
     * @param save Flag to save the setting.
     */
    fun setAdministratorPassword(administratorPassword: String, save: Boolean) {
        Settings.administratorPassword = administratorPassword
        if (save) {
            val lines = readConfigFile()
            if (lines != null) {
                for (index in lines.indices) {
                    val line = lines[index]
                    val interpreted = line.trim { it <= ' ' }
                    if (interpreted.startsWith("#")) {
                        continue
                    }
                    val spaces = getLeadingSpaceCount(line)
                    if (interpreted.startsWith("administrator_password:")) {
                        val newLine = getSpaces(spaces) + "administrator_password: \"" + administratorPassword + "\""
                        lines[index] = newLine
                    }
                }
                writeConfigFile(lines)
            }
        }
    }

    /**
     * Sets the URL to the MongoDB server.
     *
     * @param databaseURL The URL to set.
     * @param save Flag to save the setting.
     */
    fun setDatabaseURL(databaseURL: String, save: Boolean) {
        Settings.databaseURL = databaseURL
        if (save) {
            val lines = readConfigFile()
            if (lines != null) {
                for (index in lines.indices) {
                    val line = lines[index]
                    val interpreted = line.trim { it <= ' ' }
                    if (interpreted.startsWith("#")) {
                        continue
                    }
                    val spaces = getLeadingSpaceCount(line)
                    if (interpreted.startsWith("url:")) {
                        val newLine = getSpaces(spaces) + "url: \"" + databaseURL + "\""
                        lines[index] = newLine
                    }
                }
                writeConfigFile(lines)
            }
        }
    }

    /**
     * Sets the PORT to the MongoDB server.
     *
     * @param databasePORT The PORT to set.
     * @param save Flag to save the setting.
     */
    fun setDatabasePort(databasePORT: Int, save: Boolean) {
        databasePort = databasePORT
        if (save) {
            val lines = readConfigFile()
            if (lines != null) {
                for (index in lines.indices) {
                    val line = lines[index]
                    val interpreted = line.trim { it <= ' ' }
                    if (interpreted.startsWith("#")) {
                        continue
                    }
                    val spaces = getLeadingSpaceCount(line)
                    if (interpreted.startsWith("port:")) {
                        val newLine = getSpaces(spaces) + "port: " + databasePORT
                        lines[index] = newLine
                    }
                }
                writeConfigFile(lines)
            }
        }
    }

    /**
     * Sets the user-name for the Sledgehammer account in the MongoDB server.
     *
     * @param databaseUsername The user-name to set.
     * @param save Flag to save the setting.
     */
    fun setDatabaseUsername(databaseUsername: String, save: Boolean) {
        Settings.databaseUsername = databaseUsername
        if (save) {
            val lines = readConfigFile()
            if (lines != null) {
                for (index in lines.indices) {
                    val line = lines[index]
                    val interpreted = line.trim { it <= ' ' }
                    if (interpreted.startsWith("#")) {
                        continue
                    }
                    val spaces = getLeadingSpaceCount(line)
                    if (interpreted.startsWith("username:")) {
                        val newLine = getSpaces(spaces) + "username: \"" + databaseUsername + "\""
                        lines[index] = newLine
                    }
                }
                writeConfigFile(lines)
            }
        }
    }

    /**
     * Sets the password for the Sledgehammer account in the MongoDB server.
     *
     * @param databasePassword The password to set.
     * @param save Flag to save the setting.
     */
    fun setDatabasePassword(databasePassword: String, save: Boolean) {
        Settings.databasePassword = databasePassword
        if (save) {
            val lines = readConfigFile()
            if (lines != null) {
                for (index in lines.indices) {
                    val line = lines[index]
                    val interpreted = line.trim { it <= ' ' }
                    if (interpreted.startsWith("#")) {
                        continue
                    }
                    val spaces = getLeadingSpaceCount(line)
                    if (interpreted.startsWith("password:")) {
                        val newLine = getSpaces(spaces) + "password: \"" + databasePassword + "\""
                        lines[index] = newLine
                    }
                }
                writeConfigFile(lines)
            }
        }
    }

    /**
     * Sets the database for the Sledgehammer account in the MongoDB server.
     *
     * @param databaseDatabase The database to set.
     * @param save Flag to save the setting.
     */
    fun setDatabaseDatabase(databaseDatabase: String, save: Boolean) {
        Settings.databaseDatabase = databaseDatabase
        if (save) {
            val lines = readConfigFile()
            if (lines != null) {
                for (index in lines.indices) {
                    val line = lines[index]
                    val interpreted = line.trim { it <= ' ' }
                    if (interpreted.startsWith("#")) {
                        continue
                    }
                    val spaces = getLeadingSpaceCount(line)
                    if (interpreted.startsWith("database:")) {
                        val newLine = getSpaces(spaces) + "database: \"" + databaseDatabase + "\""
                        lines[index] = newLine
                    }
                }
                writeConfigFile(lines)
            }
        }
    }

    /**
     * Sets the message sent to a Player when a permission is denied.
     *
     * @param permissionDeniedMessage The message to set.
     * @param save Flag to save the setting.
     */
    fun setPermissionDeniedMessage(permissionDeniedMessage: String, save: Boolean) {
        Settings.permissionDeniedMessage = permissionDeniedMessage
        if (save) {
            val lines = readConfigFile()
            if (lines != null) {
                for (index in lines.indices) {
                    val line = lines[index]
                    val interpreted = line.trim { it <= ' ' }
                    if (interpreted.startsWith("#")) {
                        continue
                    }
                    val spaces = getLeadingSpaceCount(line)
                    if (interpreted.startsWith("permission_message_denied:")) {
                        val newLine = (getSpaces(spaces)
                                + "permission_message_denied: \""
                                + permissionDeniedMessage
                                + "\"")
                        lines[index] = newLine
                    }
                }
                writeConfigFile(lines)
            }
        }
    }

    /** @return Returns true if the native RCON utility is allowed to run on the PZ server.
     */
    fun allowRCON(): Boolean {
        return allowRCON
    }

    /**
     * (Private Method)
     *
     * @param flag The flag to set.
     */
    private fun setAllowRCON(flag: Boolean) {
        allowRCON = flag
    }

    /**
     * (Private Method)
     *
     *
     * Saves the template of the config.yml in the Sledgehammer.jar to the server folder.
     */
    private fun saveDefaultConfig() {
        val fileConfigName = "config.yml"
        write(Sledgehammer.file, fileConfigName, File(fileConfigName))
    }

//    /** Generates a new String password for the Administrator Player account.  */
//    fun generateAdministratorPassword() {
//        println("A password has been generated for the 'admin' account.")
//        println("The password is located in the config.yml and can be ")
//        println("changed. Keep this password safe.")
//        try {
//            setAdministratorPassword(MD5.getMD5Checksum("pz_admin_" + System.nanoTime()), true)
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//    }

    /**
     * Sets the expiration time in days for inactive accounts to be removed.
     *
     * @param accountIdleExpireTime The time in days to set.
     * @param save Flag to save the Setting.
     */
    fun setAccountIdleExpireTime(accountIdleExpireTime: Int, save: Boolean) {
        Settings.accountIdleExpireTime = accountIdleExpireTime
        if (save) {
            // TODO: Implement save.
        }
    }

    /**
     * @return Returns a List of account names that are excluded from the inactive-account-removal
     * utility.
     */
    val excludedIdleAccounts: List<String>?
        get() = listAccountsExcluded

    /** @return Returns true if Helicopter events are allows on the PZ server.
     */
    fun allowHelicopters(): Boolean {
        return allowHelicopters
    }

    /**
     * @return Returns true if Lua code is allowed to override from the original code from the
     * Modules.
     */
    fun overrideLua(): Boolean {
        return overrideLua
    }

    fun overrideLang(): Boolean {
        return overrideLang
    }

    /** The Java OS definition for the new-line operator.  */
    private val NEW_LINE = System.getProperty("line.separator")

    /**
     * Reads the config.yml file as a List of lines.
     *
     * @return Returns a List of lines.
     */
    private fun readConfigFile(): MutableList<String>? {
        try {
            val fr = FileReader("config.yml")
            val br = BufferedReader(fr)
            val lines: MutableList<String> = ArrayList()
            var line: String
            while (br.readLine().also { line = it } != null) {
                lines.add(line)
            }
            br.close()
            fr.close()
            return lines
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    /**
     * Writes the config.yml File with a List of lines.
     *
     * @param lines The List of lines to save.
     */
    private fun writeConfigFile(lines: List<String>) {
        try {
            val fw = FileWriter("config.yml")
            val bw = BufferedWriter(fw)
            for (line in lines) {
                bw.write(line + NEW_LINE)
            }
            bw.close()
            fw.close()
        } catch (e: IOException) {
            System.err.println("Failed to save config.yml")
            e.printStackTrace()
        }
    }

    /**
     * @param object The Object being interpreted.
     * @return Returns true if the Object identifies as a literal boolean primitive, a packaged
     * Boolean Object, or a String that matches "1", "true", "yes", or "on".
     */
    private fun getBoolean(`object`: Any): Boolean {
        return if (`object` is Boolean) {
            `object`
        } else {
            val s = `object`.toString()
            (s == "1" || s.equals("true", ignoreCase = true)
                    || s.equals("yes", ignoreCase = true)
                    || s.equals("on", ignoreCase = true))
        }
    }

    /**
     * @param length The count of spaces to add to the returned String.
     * @return Returns a valid YAML character sequence of spaces as a String.
     */
    private fun getSpaces(length: Int): String {
        require(length >= 0) { "length given is less than 0." }
        val string = StringBuilder()
        for (index in 0 until length) {
            string.append(" ")
        }
        return string.toString()
    }

    /**
     * @param string The String being interpreted.
     * @return Returns the count of spaces in front of any text in the given line.
     */
    private fun getLeadingSpaceCount(string: String?): Int {
        requireNotNull(string) { "String given is null." }
        if (string.isEmpty()) {
            return 0
        }
        var spaces = 0
        val chars = string.toCharArray()
        for (c in chars) {
            if (c != ' ') {
                break
            }
            spaces++
        }
        return spaces
    }

    /**
     * @return Returns input from the console.
     */
    private fun requestDatabaseURL(): String {
        var databaseURL: String? = null
        var input: String
        // Cannot close this. It closes the System.in entirely.
        val scanner = Scanner(System.`in`)
        while (databaseURL == null) {
            println("Please enter the MongoDB URL: (localhost)")
            input = scanner.nextLine()
            databaseURL = if (!input.isEmpty()) {
                input
            } else {
                "localhost"
            }
        }
        return databaseURL
    }

    /**
     * @return Returns input from the console.
     */
    private fun requestDatabasePORT(): Int {
        var databasePORT: Int? = null
        var input: String
        // Cannot close this. It closes the System.in entirely.
        val scanner = Scanner(System.`in`)
        while (databasePORT == null) {
            println("Please enter the MongoDB url: (27017)")
            input = scanner.nextLine()
            if (!input.isEmpty()) {
                try {
                    val value = input.toInt()
                    if (value <= 0) {
                        println(
                            "The PORT provided is 0 or less than 0 and needs to be a non-negative integer not exceeding 65534.")
                        continue
                    } else if (value > 65534) {
                        println(
                            "The PORT provided is greater than 65534 and needs to be a non-negative integer not exceeding 65534.")
                        continue
                    }
                    databasePORT = value
                } catch (e: NumberFormatException) {
                    println(
                        "The PORT provided is invalid and needs to be a non-negative integer not exceeding 65534.")
                }
            } else {
                databasePORT = 27017
            }
        }
        return databasePORT
    }

    /**
     * @return Returns input from the console.
     */
    private fun requestDatabaseUsername(): String {
        var databaseUsername: String? = null
        var input: String
        // Cannot close this. It closes the System.in entirely.
        val scanner = Scanner(System.`in`)
        while (databaseUsername == null) {
            println("Please enter the MongoDB username: (sledgehammer)")
            input = scanner.nextLine()
            databaseUsername = if (!input.isEmpty()) {
                input
            } else {
                "sledgehammer"
            }
        }
        return databaseUsername
    }

    /**
     * @return Returns input from the console.
     */
    private fun requestDatabasePassword(): String {
        var databasePassword: String? = null
        var input: String
        // Cannot close this. It closes the System.in entirely.
        val scanner = Scanner(System.`in`)
        while (databasePassword == null) {
            println("Please enter the MongoDB password:")
            input = scanner.nextLine()
            if (!input.isEmpty()) {
                databasePassword = input
            } else {
                println("This is not a valid password.")
            }
        }
        return databasePassword
    }

    /**
     * @return Returns input from the console.
     */
    private fun requestDatabaseDatabase(): String {
        var databaseDatabase: String? = null
        var input: String
        // Cannot close this. It closes the System.in entirely.
        val scanner = Scanner(System.`in`)
        while (databaseDatabase == null) {
            println("Please enter the MongoDB database: (sledgehammer)")
            input = scanner.nextLine()
            databaseDatabase = if (!input.isEmpty()) {
                input
            } else {
                "sledgehammer"
            }
        }
        return databaseDatabase
    }

    /**
     * @return Returns input from the console.
     */
    private fun requestPZDedicatedServerDirectory(): String {
        var pzDirectory: String? = null
        var input: String
        // Cannot close this. It closes the System.in entirely.
        val scanner = Scanner(System.`in`)
        while (pzDirectory == null) {
            println("Please enter the directory for the Project Zomboid Dedicated Server:")
            input = scanner.nextLine()
            println("nextLine: $input")
            input = input.replace("\\", "/")
            if (!input.endsWith("/")) input += "/"
            val directory = File(input)
            println("File: $directory")
            if (directory.exists() && directory.isDirectory) {
                val zombieDirectory = File(input + File.separator + "java" + File.separator + "zombie")
                if (zombieDirectory.exists() && zombieDirectory.isDirectory) {
                    pzDirectory = input
                } else {
                    println("This is a directory, but it does not contain Project Zomboid files.")
                }
            } else {
                println("This is not a valid directory.")
            }
        }
        return pzDirectory.replace("\\", "/")
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
}