package jab.sledgehammer.util

import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.DumperOptions.ScalarStyle
import org.yaml.snakeyaml.Yaml

/**
 * Utility class for Yaml operations.
 *
 * @author Jab
 */
object YamlUtil {

    /**
     * @return Returns the global instance of Yaml.
     */
    val yaml: Yaml

    init {
        val options = DumperOptions()
        options.defaultFlowStyle = DumperOptions.FlowStyle.BLOCK
        options.defaultScalarStyle = ScalarStyle.LITERAL
        yaml = Yaml(options)
    }
}
