package com.asledgehammer.framework.cfg

import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.Yaml
import java.io.*

class YamlFile(file: File? = null) : CFGFile(file) {

    /**
     * Parse and read from a YAML file.
     *
     * @param file The file to process.
     */
    fun read(): CFGFile = read(FileInputStream(file))

    /**
     * Parse and read from a InputStream.
     *
     * @param inputStream The stream to parse and read.
     */
    fun read(inputStream: InputStream): CFGFile {
        @Suppress("UNCHECKED_CAST")
        read(yaml.load(inputStream) as Map<String, Any>)
        return this
    }

    fun read(yaml: String): CFGFile {
        read(YamlFile.yaml.load(yaml) as Map<String, Any>);
        return this
    }

    /**
     * Writes the section to a YAML file.
     *
     * @param file The file to write.
     * @param overwrite Set this to true to overwrite a file if it already exists.
     */
    fun write(file: File, overwrite: Boolean = false) {
        if (!overwrite && file.exists()) return
        val bos = BufferedOutputStream(FileOutputStream(file))
        val writer = bos.writer()
        val string = yaml.dump(toMap())
        println(string)
        writer.write(string)
        writer.flush()
        writer.close()
        bos.flush()
        bos.close()
    }

    @Suppress("UNCHECKED_CAST")
    private fun read(map: Map<String, Any>) {

        fun recurse(name: String, map: Map<String, Any>): CFGSection {

            val section = CFGSection(name)

            for ((key, value) in map) {
                if (value is Map<*, *>) section.set(key, recurse(key, value as Map<String, Any>))
                else section.set(key, value)
            }

            return section
        }

        for ((key, value) in map) {
            if (value is Map<*, *>) set(key, recurse(key, value as Map<String, Any>))
            else set(key, value)
        }
    }

    companion object {

        /**
         * The SnakeYAML API to parse and compile YAML.
         */
        val yaml: Yaml

        init {
            val dumperOptions = DumperOptions()
            dumperOptions.defaultFlowStyle = DumperOptions.FlowStyle.BLOCK
            dumperOptions.isAllowUnicode = true
            yaml = Yaml(dumperOptions)
        }
    }
}