package com.malinskiy.marathon.extensions

import com.android.build.gradle.api.ApkVariantOutput
import com.android.build.gradle.api.ApplicationVariant
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.api.LibraryVariant
import com.android.build.gradle.api.LibraryVariantOutput
import com.android.build.gradle.tasks.PackageAndroidArtifact
import java.io.File

fun BaseVariant.extractApplication(): File? =
    executeGradleCompat(
        exec = {
            extractApplication3_3_plus(this)
        },
        fallbacks = listOf {
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
        val apppackageAndroidArtifact: PackageAndroidArtifact = applicationProvider.get()
        assert(apppackageAndroidArtifact.variantOutputs.get().size == 1)
        File(
            apppackageAndroidArtifact.outputDirectory.asFile.get(),
            apppackageAndroidArtifact.variantOutputs.get().first().outputFileName.get()
        )
    }
}

@Suppress("DEPRECATION")
private fun extractApplicationBefore3_3(output: BaseVariant): File? {
    val variantOutput = output.outputs.first()
    return when (variantOutput) {
        is ApkVariantOutput -> {
            File(variantOutput.packageApplication.outputDirectory.asFile.get(), variantOutput.outputFileName)
        }
        is LibraryVariantOutput -> {
            null
        }
        else -> {
            throw RuntimeException("Can't find apk")
        }
    }
}
