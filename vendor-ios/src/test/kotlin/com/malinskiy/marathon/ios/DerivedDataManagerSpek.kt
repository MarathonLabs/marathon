package com.malinskiy.marathon.ios

import com.malinskiy.marathon.execution.Configuration
import com.malinskiy.marathon.report.html.relativePathTo
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import mu.KotlinLogging
import org.amshove.kluent.shouldEqual
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.testcontainers.containers.BindMode
import org.testcontainers.containers.GenericContainer
import java.io.File
import java.util.*

private val logger = KotlinLogging.logger { }

// https://github.com/testcontainers/testcontainers-java/issues/318
class KGenericContainer(imageName: String) : GenericContainer<KGenericContainer>(imageName)

class DerivedDataManagerSpek: Spek({
    describe("DerivedDataManager") {
        val device: IOSDevice = mock()
        whenever(device.udid).thenReturn(UUID.randomUUID().toString())

        val privateKey = File(javaClass.classLoader.getResource("src/test_rsa").file)
        val publicKeyResourcePath = "src/test_rsa.pub"

        val container = KGenericContainer("axiom/rsync-server")
                .withClasspathResourceMapping(publicKeyResourcePath, "/root/.ssh/authorized_keys", BindMode.READ_WRITE)
                .withExposedPorts(22, 873)

        container.start()

        val containerHost = container.containerIpAddress
        val sshPort = container.getMappedPort(22)

        given("what follows") {
            val sourceRoot = File(javaClass.classLoader.getResource("src/sample-xcworkspace/sample-appUITests").file)
            val derivedDataPath = File(javaClass.classLoader.getResource("build").file)
            val xctestrunPath = File(javaClass.classLoader.getResource("src/UITesting_iphonesimulator11.2-x86_64.xctestrun").file)
            val configuration = Configuration(name = "",
                    outputDir = File(""),
                    analyticsConfiguration = null,
                    poolingStrategy = null,
                    shardingStrategy = null,
                    sortingStrategy = null,
                    batchingStrategy = null,
                    flakinessStrategy = null,
                    retryStrategy = null,
                    filteringConfiguration = null,
                    ignoreFailures = null,
                    isCodeCoverageEnabled = null,
                    fallbackToScreenshots = null,
                    testClassRegexes = null,
                    includeSerialRegexes = null,
                    excludeSerialRegexes = null,
                    testOutputTimeoutMillis = null,
                    debug = null,
                    vendorConfiguration =  IOSConfiguration(derivedDataPath, xctestrunPath, "root", privateKey, sourceRoot)
            )

            it("should send all files") {
                val manager = DerivedDataManager(
                        configuration = configuration,
                        hostname = containerHost,
                        sshPort = sshPort
                )

                val productsDir = File(derivedDataPath.absolutePath + File.separator + "Build/Products/")
                val remoteDir = "/data/${device.udid}/"

                // Upload
                manager.send(localDir = productsDir,
                        remoteDir = remoteDir)

                val uploadResults = container.execInContainer("/usr/bin/find", remoteDir).stdout
                        .split("\n")
                        .filter { it.isNotEmpty() }
                        .map { File(it).relativePathTo(File(remoteDir)) }
                        .toSet()

                val expectedFiles = productsDir.walkTopDown().map { it.relativePathTo(productsDir) }.toSet()

                uploadResults.size shouldEqual expectedFiles.size
                uploadResults shouldEqual expectedFiles
            }

            it("should receive all files") {

                val manager = DerivedDataManager(
                        configuration = configuration,
                        hostname = containerHost,
                        sshPort = sshPort
                )

                val productsDir = File(derivedDataPath.absolutePath + File.separator + "Build/Products/")
                val remoteDir = "/data/${device.udid}/"

                // Download
                val tempDir = createTempDir()

                manager.receive(remoteDir = remoteDir, localDir = tempDir)

                val tempFiles = tempDir.walkTopDown().map { it.relativePathTo(tempDir) }.toSet()
                val expectedFiles = productsDir.walkTopDown().map { it.relativePathTo(productsDir) }.toSet()

                // Compare
                tempFiles.size shouldEqual expectedFiles.size
                tempFiles shouldEqual expectedFiles

                tempDir.deleteRecursively()
            }
        }
    }
})
