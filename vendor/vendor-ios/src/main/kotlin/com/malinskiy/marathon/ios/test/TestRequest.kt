package com.malinskiy.marathon.ios.test

import com.malinskiy.marathon.test.Test

data class TestRequest(
    val workdir: String,
    val xctestrun: String,
    val tests: List<Test>,
    val xcresult: String,
    val coverage: Boolean,
) {
    fun toXcodebuildTestFilter(): Array<String> {
        return tests.map { "'-only-testing:${it.pkg}/${it.clazz}/${it.method}'" }.toTypedArray()
    }
}
    
