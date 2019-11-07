package com.malinskiy.marathon.android

import com.android.SdkConstants.FN_LOCAL_PROPERTIES
import com.google.common.base.Charsets
import com.google.common.io.Closeables
import org.gradle.api.Project
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStreamReader
import java.util.*

val Project.androidSdkLocation: File
    get() {
        val rootDir = project.rootDir
        val localProperties = File(rootDir, FN_LOCAL_PROPERTIES)
        val properties = Properties()

        if (localProperties.isFile) {
            var reader: InputStreamReader? = null
            try {
                val fis = FileInputStream(localProperties)
                reader = InputStreamReader(fis, Charsets.UTF_8)
                properties.load(reader)
            } catch (ignored: FileNotFoundException) {
            } catch (e: IOException) {
                throw RuntimeException(
                    String.format("Unable to read %1\$s.", localProperties.absolutePath), e
                )
            } finally {
                try {
                    Closeables.close(reader, true)
                } catch (e: IOException) {
                    // ignore.
                }
            }
        }

        return findSdkLocation(properties, rootDir)
            ?: throw RuntimeException("SDK location not found. Define location with sdk.dir in the local.properties file or with an ANDROID_HOME environment variable.")
    }

private fun findSdkLocation(properties: Properties, rootDir: File): File? {
    var sdkDirProp: String? = properties.getProperty("sdk.dir")
    if (sdkDirProp != null) {
        var sdk = File(sdkDirProp)
        if (!sdk.isAbsolute) {
            sdk = rootDir.resolve(sdkDirProp)
        }
        return sdk
    }

    sdkDirProp = properties.getProperty("android.dir")
    if (sdkDirProp != null) {
        return rootDir.resolve(sdkDirProp)
    }

    val envVar = System.getenv("ANDROID_HOME")
    if (envVar != null) {
        var sdk = File(envVar)
        if (!sdk.isAbsolute) {
            sdk = rootDir.resolve(envVar)
        }
        return sdk
    }

    val property = System.getProperty("android.home")
    return when {
        property != null -> File(property)
        else -> null
    }
}
