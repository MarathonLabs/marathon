package com.malinskiy.marathon.io

import com.google.common.io.Files
import com.malinskiy.marathon.device.DeviceFeature
import com.malinskiy.marathon.device.DeviceInfo
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.device.NetworkState
import com.malinskiy.marathon.device.OperatingSystem
import com.malinskiy.marathon.extension.escape
import com.malinskiy.marathon.test.toTestName
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test
import java.io.File

class FileManagerTest {
    private val output = Files.createTempDir()
    private val fileManager = FileManager(output)

    private companion object {
        val poolId = DevicePoolId("testPoolId")
        val deviceInfo = DeviceInfo(
            operatingSystem = OperatingSystem("23"),
            serialNumber = "xxyyzz",
            model = "Android SDK built for x86",
            manufacturer = "unknown",
            networkState = NetworkState.CONNECTED,
            deviceFeatures = listOf(DeviceFeature.SCREENSHOT, DeviceFeature.VIDEO),
            healthy = true
        )
        val shortNameTest = com.malinskiy.marathon.test.Test(
            "com.example",
            "Clazz",
            "method",
            emptyList()
        )

        val longNameTest = com.malinskiy.marathon.test.Test(
            "com.example",
            "Clazz",
            "pefalgkxbnfrvxsdprtoprggsibqaeobgiyvaiatysajemcoamubppvppibyuknyeqhipkvzwyesngazgycpkwzahyocsfsxclfgaxvpwrsrjukwrtlwpreewlvekqvkibvtwlizaxwwvzbgfepwypiwpbfgzchjrytvzawoulupuitbxkgchjihbbjdacohxleojtcMyRandomTextEndingWithMoreThan256CharactersLengthToVerifyCutPoint",
            emptyList()
        )

        val longNamedParameterizedTest = com.malinskiy.marathon.test.Test(
            "com.example",
            "Clazz",
            "someRidiculousLongLongLongLongLongLongLongLongLongLongLongLongLongLongLongNamepefalgkxbnfrvxsdprtoprggsibqaeobgiyvaiatysajemcoamubppvppibyuknyeqhipkvzwyesngazgycpkwzah[Some parameter with spaces: /rest/api/2/endpoint, Other parameter: 1, And another one: {3}]",
            emptyList()
        )

        val batchId = "batchId"
    }

    @Test
    fun createFilenameNormalLengthTest() {
        val file = fileManager.createFile(FileType.LOG, poolId, deviceInfo, shortNameTest, batchId)
        file.name shouldBeEqualTo "com.example.Clazz#method-batchId.log"
    }

    @Test
    fun createFilenameLongLengthMethodTest() {
        val file = fileManager.createFile(FileType.LOG, poolId, deviceInfo, longNameTest, batchId)
        file.absolutePath.length shouldBeEqualTo 255
        val filenameLimit = 255 - file.parentFile.absolutePath.length - File.separator.length
        val fqtnLimit = filenameLimit - "-${batchId}.log".length
        file.name shouldBeEqualTo "${longNameTest.toTestName().escape().take(fqtnLimit)}-${batchId}.log"
    }

    @Test
    fun testCreateFilenameNamedParameterizedLong() {
        val file = fileManager.createFile(FileType.LOG, poolId, deviceInfo, longNamedParameterizedTest, batchId)
        file.absolutePath.length shouldBeEqualTo 255
        val filenameLimit = 255 - file.parentFile.absolutePath.length - File.separator.length
        val fqtnLimit = filenameLimit - "-${batchId}.log".length
        file.name shouldBeEqualTo "${longNamedParameterizedTest.toTestName().escape().take(fqtnLimit)}-${batchId}.log"
    }
}
