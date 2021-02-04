package com.malinskiy.marathon.config

import org.yaml.snakeyaml.Yaml
import java.io.File

class ConfigGenerator {
    fun saveConfig(config: Config, file: File) {
        val serializer = Yaml()
        serializer.dump(config, file.writer())
    }
}
