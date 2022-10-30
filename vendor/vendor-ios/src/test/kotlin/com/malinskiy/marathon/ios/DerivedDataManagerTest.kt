package com.malinskiy.marathon.ios

import com.malinskiy.marathon.config.Configuration
import com.malinskiy.marathon.config.vendor.VendorConfiguration
import com.malinskiy.marathon.extension.relativePathTo
import com.malinskiy.marathon.log.MarathonLogging
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.reset
import com.nhaarman.mockitokotlin2.whenever
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldNotBeEqualTo
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.testcontainers.containers.BindMode
import org.testcontainers.containers.GenericContainer
import java.io.File
import java.nio.file.Files
import java.nio.file.attribute.PosixFilePermissions
import java.util.UUID

class DerivedDataManagerTest {
    private val device: IOSDevice = mock()
    private val publicKeyResourcePath = "fixtures/derived-data-manager/test_rsa.pub"

    // https://github.com/testcontainers/testcontainers-java/issues/318
    class KGenericContainer(imageName: String) : GenericContainer<KGenericContainer>(imageName)

    private lateinit var container: KGenericContainer
    private lateinit var containerHost: String
    private var sshPort: Int = 0
    private val sourceRoot =
        File(javaClass.classLoader.getResource("sample-xcworkspace/sample-appUITests").file)
    private val derivedDataDir =
        File(javaClass.classLoader.getResource("sample-xcworkspace/derived-data").file)
    private val xctestrunPath =
        File(javaClass.classLoader.getResource("sample-xcworkspace/derived-data/Build/Products/UITesting_iphonesimulator11.2-x86_64.xctestrun").file)
    private val configuration = Configuration.Builder(
        name = "",
        outputDir = File(""),
    ).apply {
        debug = false
        analyticsTracking = false
        vendorConfiguration = VendorConfiguration.IOSConfiguration(
            derivedDataDir = derivedDataDir,
            xctestrunPath = xctestrunPath,
            remoteUsername = "root",
            remotePrivateKey = privateKey,
            knownHostsPath = null,
            remoteRsyncPath = "/usr/bin/rsync",
            sourceRoot = sourceRoot,
            debugSsh = false,
            alwaysEraseSimulators = true
        )
    }.build()

    @BeforeEach
    fun `setup mocks`() {
        reset(device)
        whenever(device.udid).thenReturn(UUID.randomUUID().toString())
    }

    @BeforeEach
    fun `setup resync`() {
        container = KGenericContainer("axiom/rsync-server")
            .withClasspathResourceMapping(
                publicKeyResourcePath,
                "/root/.ssh/authorized_keys",
                BindMode.READ_WRITE
            )
            .withExposedPorts(22, 873)

        container.start()

        containerHost = container.containerIpAddress
        sshPort = container.getMappedPort(22)
        sshPort shouldNotBeEqualTo 0
    }

    @AfterEach
    fun `stop rsync`() {
        container.stop()
    }

    companion object {
        val logger = MarathonLogging.logger(javaClass.simpleName)
        val privateKey = File(javaClass.classLoader.getResource("fixtures/derived-data-manager/test_rsa").file)

        @BeforeAll
        @JvmStatic
        fun `setup permissions`() {
            try {
                Files.setPosixFilePermissions(
                    privateKey.toPath(),
                    PosixFilePermissions.fromString("rw-------")
                )
            } catch (e: Exception) {
            }
            logger.debug { "Using private key $privateKey" }
        }
    }

    @Test
    fun `should determine products location`() {
        val manager = DerivedDataManager(configuration = configuration)

        manager.productsDir shouldBeEqualTo derivedDataDir.resolve("Build/Products/")
    }

    @Test
    fun `should send and receive all files`() {
        val manager = DerivedDataManager(configuration = configuration)

        val productsDir = manager.productsDir
        val remoteDir = "/data/${device.udid}/"

        // Upload
        manager.send(
            localPath = productsDir,
            remotePath = remoteDir,
            hostName = containerHost,
            port = sshPort
        )

        val stdout = container.execInContainer("/usr/bin/find", remoteDir).stdout
        logger.debug { stdout }

        val uploadResults = stdout
            /**
             * Workaround for cases where find inserts \n in the middle of path
             */
            .split("\n/")
            .map { "/" + it.replace("\n", "") }
            .filter { it.isNotEmpty() }
            .map { File(it).relativePathTo(File(remoteDir)) }
            .toSet()

        val expectedUploadFiles =
            productsDir.walkTopDown().map { it.relativePathTo(productsDir) }.toSet()

        uploadResults shouldBeEqualTo expectedUploadFiles

        // Download
        val tempDir = createTempDir()

        manager.receive(
            remotePath = remoteDir,
            localPath = tempDir,
            hostName = containerHost,
            port = sshPort
        )

        val tempFiles = tempDir.walkTopDown().map { it.relativePathTo(tempDir) }.toSet()
        val expectedDownloadFiles =
            productsDir.walkTopDown().map { it.relativePathTo(productsDir) }.toSet()

        // Compare
        tempFiles shouldBeEqualTo expectedDownloadFiles

        tempDir.deleteRecursively()
    }
}
