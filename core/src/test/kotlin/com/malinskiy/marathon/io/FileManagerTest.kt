package com.malinskiy.marathon.io

import com.malinskiy.marathon.device.*
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test
import java.io.File

class FileManagerTest {
    private val file = File("testFile")
    private val fileManager = FileManager(file)

    private companion object{
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
            emptyList())

        val longNameTest = com.malinskiy.marathon.test.Test(
            "com.example",
            "Clazz",
            "pefalgkxbnfrvxsdprtoprggsibqaeobgiyvaiatysajemcoamubppvppibyuknyeqhipkvzwyesngazgycpkwzahyocsfsxclfgaxvpwrsrjukwrtlwpreewlvekqvkibvtwlizaxwwvzbgfepwypiwpbfgzchjrytvzawoulupuitbxkgchjihbbjdacohxleojtcMyRandomTextEndingWithMoreThan256CharactersLengthToVerifyCutPoint",
            emptyList())

        val batchId = "batchId"
    }

    @Test
    fun createFilenameNormalLengthTest(){
        val file = fileManager.createFile(FileType.LOG, poolId, deviceInfo, shortNameTest, batchId)
        file.name shouldBeEqualTo "com.example.Clazz#method-batchId.log"
    }

    @Test
    fun createFilenameLongLengthMethodTest(){
        val file = fileManager.createFile(FileType.LOG, poolId, deviceInfo, longNameTest, batchId)
        file.name.length shouldBeEqualTo 256
        file.name shouldBeEqualTo "com.example.Clazz#pefalgkxbnfrvxsdprtoprggsibqaeobgiyvaiatysajemcoamubppvppibyuknyeqhipkvzwyesngazgycpkwzahyocsfsxclfgaxvpwrsrjukwrtlwpreewlvekqvkibvtwlizaxwwvzbgfepwypiwpbfgzchjrytvzawoulupuitbxkgchjihbbjdacohxleojtcMyRandomTextEndingWithMoreT-batchId.log"
    }
}
