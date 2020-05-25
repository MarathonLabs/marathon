package com.malinskiy.marathon.ios.idb.grpc

import java.io.File

sealed class TestType{
    object XCTest : TestType()
    object XcTestRun : TestType()
}

class XcTest {
    private fun extractType(bundle: File) : TestType{
        return when (bundle.extension) {
            "xctestrun" -> {
                if(bundle.isFile) TestType.XcTestRun
                else throw IllegalStateException()
            }
            "xctest" -> {
                if(bundle.isDirectory) TestType.XCTest
                else throw IllegalStateException()
            }
            else -> throw IllegalArgumentException()
        }
    }

    fun parse(bundle: File) : List<File>{
        val type = extractType(bundle)
        if(type == TestType.XCTest){
            return listOf(bundle)
        }
        return XcTestRunParser().extractArtifacts(bundle).map { File(it) }
    }
}
