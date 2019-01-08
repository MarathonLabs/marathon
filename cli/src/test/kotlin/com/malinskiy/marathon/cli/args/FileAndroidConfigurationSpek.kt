package com.malinskiy.marathon.cli.args

import com.malinskiy.marathon.exceptions.ConfigurationException
import org.amshove.kluent.shouldEqual
import org.amshove.kluent.shouldThrow
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import java.io.File

object FileAndroidConfigurationSpek : Spek({
    describe("FileAndroidConfiguration") {
        val configuration by memoized {
            FileAndroidConfiguration(
                    null,
                    null,
                    File.createTempFile("foo", "bar"),
                    null,
                    null
            )
        }

        val env = File.createTempFile("foo", "bar")
        val sdk = File.createTempFile("android", "sdk")

        group("androidSdk is null") {
            it("should throw Exception if env android sdk also is null") {
                { configuration.toAndroidConfiguration(null) } shouldThrow ConfigurationException::class
            }
            it("should use env android sdk if it is not null") {
                configuration.toAndroidConfiguration(env).androidSdk shouldEqual env
            }
        }
        group("android sdk is not null") {
            it("should use android sdk instead of env if both exists") {
                configuration.copy(androidSdk = sdk).toAndroidConfiguration(env).androidSdk shouldEqual sdk
            }
        }
        group("test application output") {
            it("should be null by default") {
                configuration.toAndroidConfiguration(env).applicationOutput shouldEqual null
            }
            it("should be null if provided") {
                configuration.copy(applicationOutput = env).toAndroidConfiguration(env).applicationOutput shouldEqual env
            }
        }
        group("test application apk") {
            it("should be equal") {
                configuration.copy(testApplicationOutput = env).toAndroidConfiguration(env).testApplicationOutput shouldEqual env
            }
        }
        group("auto grant permissions") {
            it("should be false by default") {
                configuration.toAndroidConfiguration(env).autoGrantPermission shouldEqual false
            }
            it("should be equal") {
                configuration.copy(autoGrantPermission = false).toAndroidConfiguration(env).autoGrantPermission shouldEqual false
                configuration.copy(autoGrantPermission = true).toAndroidConfiguration(env).autoGrantPermission shouldEqual true
            }
        }
        group("adb init timeout millis") {
            it("should be 30_000 by default") {
                configuration.toAndroidConfiguration(env).adbInitTimeoutMillis shouldEqual 30_000
            }
            it("should be equal") {
                val timeout = 500_000
                configuration.copy(adbInitTimeoutMillis = timeout).toAndroidConfiguration(env).adbInitTimeoutMillis shouldEqual timeout
            }
        }
    }
})
