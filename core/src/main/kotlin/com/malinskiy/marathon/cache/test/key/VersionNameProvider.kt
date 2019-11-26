package com.malinskiy.marathon.cache.test.key

class VersionNameProvider {
    val versionName: String = VersionNameProvider::class.java.`package`.implementationVersion
}
