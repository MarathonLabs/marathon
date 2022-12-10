package com.malinskiy.marathon.ios.test

import com.malinskiy.marathon.test.Test

data class TestRequest(
    val workdir: String,
    val xctestrun: String,
    val tests: List<Test>,
    val xcresult: String,
) {
    fun toXcodebuildTestFilter(): String {
        return tests.joinToString(separator = " ") { "-only-testing:\"${it.pkg}/${it.clazz}/${it.method}\"" }
    }
}
    
