package com.malinskiy.marathon.config.serialization.yaml

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import java.io.File
import java.nio.file.Paths

class FileDeserializer(private val marathonfileDir: File) : StdDeserializer<File>(File::class.java) {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): File {
        val path = Paths.get(p.valueAsString)
        return if (path.isAbsolute) {
            path.toFile()
        } else {
            File(marathonfileDir, p.valueAsString).canonicalFile
        }
    }
}
