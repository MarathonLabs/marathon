package com.malinskiy.marathon.extensions

import com.android.build.gradle.api.ApkVariantOutput
import com.android.build.gradle.api.ApplicationVariant
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.api.LibraryVariant
import com.android.build.gradle.api.LibraryVariantOutput
import java.io.File

fun BaseVariant.extractApplication(): File? =
        executeGradleCompat(
                exec = {
                    extractApplication3_3_plus(this)
                },
                fallback = {
                    extractApplicationBefore3_3(this)
                }
        )

private fun extractApplication3_3_plus(output: BaseVariant): File? {
    val applicationProvider = when (output) {
        is ApplicationVariant -> {
            output.packageApplicationProvider
        }
        is LibraryVariant -> {
            null
        }
        else -> {
            throw RuntimeException("Can't find application provider. Output is ${output.javaClass.canonicalName}")
        }
    }

    return applicationProvider?.let {
        val apppackageAndroidArtifact = applicationProvider.get()
        assert(apppackageAndroidArtifact.apkNames.size == 1)
        File(apppackageAndroidArtifact.outputDirectory, apppackageAndroidArtifact.apkNames.first())
    } ?: null
}

@Suppress("DEPRECATION")
private fun extractApplicationBefore3_3(output: BaseVariant): File? {
    val variantOutput = output.outputs.first()
    return when (variantOutput) {
        is ApkVariantOutput -> {
            File(variantOutput.getPackageApplication().outputDirectory.path, variantOutput.outputFileName)
        }
        is LibraryVariantOutput -> {
            null
        }
        else -> {
            throw RuntimeException("Can't find apk")
        }
    }
}