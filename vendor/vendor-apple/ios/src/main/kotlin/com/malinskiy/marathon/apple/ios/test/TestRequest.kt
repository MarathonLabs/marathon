package com.malinskiy.marathon.apple.ios.test

import com.malinskiy.marathon.test.Test
import com.malinskiy.marathon.test.toTestName

data class TestRequest(
    val workdir: String,
    val remoteXctestrun: String,
    val coverage: Boolean,
    val tests: List<Test>? = null,
    val xcresult: String? = null,
    val testTargetName: String? = null,
) {
    fun toXcodebuildTestFilter(): Array<String> {
        return tests?.map { "'-only-testing:${it.toTestName(packageSeparator = '/', methodSeparator = '/')}'" }?.toTypedArray() ?: emptyArray()
    }
}
    
