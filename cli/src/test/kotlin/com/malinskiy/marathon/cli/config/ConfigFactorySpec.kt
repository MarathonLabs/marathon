package com.malinskiy.marathon.cli.config

import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import java.io.File

object ConfigFactorySpec: Spek({
    given("ConfigFactory") {
        val parser = ConfigFactory()

        on("sample config 1") {
            val file = File(ConfigFactorySpec::class.java.getResource("/fixture/config/sample_1.yaml").file)

            it("should deserialize") {
                val configuration = parser.create(file, File("/local/android"))
            }
        }
    }
})
