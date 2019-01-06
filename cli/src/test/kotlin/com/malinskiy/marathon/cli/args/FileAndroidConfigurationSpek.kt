package com.malinskiy.marathon.cli.args

import com.malinskiy.marathon.exceptions.ConfigurationException
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.context
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

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
                assertFailsWith(ConfigurationException::class) {
                    configuration.toAndroidConfiguration(null)
                }
            }
            it("should use env android sdk if it is not null") {
                assertEquals(env, configuration.toAndroidConfiguration(env).androidSdk)
            }
        }
        group("android sdk is not null") {
            it("should use android sdk instead of env if both exists") {
                assertEquals(sdk, configuration.copy(androidSdk = sdk).toAndroidConfiguration(env).androidSdk)
            }
        }
        group("test application output") {
            it("should be null by default") {
                assertNull(configuration.toAndroidConfiguration(env).applicationOutput)
            }
            it("should be null if provided") {
                assertEquals(env, configuration.copy(applicationOutput = env).toAndroidConfiguration(env).applicationOutput)
            }
        }
        group("test application apk") {
            it("should be equal") {
                assertEquals(env, configuration.copy(testApplicationOutput = env).toAndroidConfiguration(env).testApplicationOutput)
            }
        }
        group("auto grant permissions") {
            it("should be false by default") {
                assertEquals(false, configuration.toAndroidConfiguration(env).autoGrantPermission)
            }
            it("should be equal") {
                assertEquals(false, configuration.copy(autoGrantPermission = false).toAndroidConfiguration(env).autoGrantPermission)
                assertEquals(true, configuration.copy(autoGrantPermission = true).toAndroidConfiguration(env).autoGrantPermission)
            }
        }
        group("adb init timeout millis") {
            it("should be 30_000 by default") {
                assertEquals(30_000, configuration.toAndroidConfiguration(env).adbInitTimeoutMillis)
            }
            it("should be equal") {
                val timeout = 500_000
                assertEquals(timeout, configuration.copy(adbInitTimeoutMillis = timeout).toAndroidConfiguration(env).adbInitTimeoutMillis)
            }
        }
    }
})
