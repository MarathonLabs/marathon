package com.malinskiy.marathon.gradle.extensions

import com.android.build.gradle.api.ApkVariantOutput
import com.android.build.gradle.api.LibraryVariant
import com.android.build.gradle.api.LibraryVariantOutput
import com.android.build.gradle.api.TestVariant
import com.android.build.gradle.tasks.PackageAndroidArtifact
import org.apache.tools.ant.taskdefs.Zip
import org.gradle.api.GradleException
import java.io.File

fun TestVariant.extractTestApplication() = executeGradleCompat(
    exec = {
        extractTestApplication3_6_plus(this)
    },
    fallbacks = listOf(
        {
            extractTestApplication3_3_to_3_5(this)
        },
        {
            extractTestApplicationBefore3_3(this)
        }
    )
)

fun extractTestApplication3_6_plus(variant: TestVariant): File {
    val output = variant.outputs.first()

    return File(
        when (output) {
            is ApkVariantOutput -> {
                val packageTask =
                    variant.packageApplicationProvider.orNull ?: throw IllegalArgumentException("Can't find package application provider")
                File(packageTask.outputDirectory.asFile.get(), output.outputFileName).path
            }
            is LibraryVariantOutput -> {
                output.outputFile.path
            }
            else -> {
                throw RuntimeException("Can't find instrumentationApk")
            }
        }
    )
}

private fun extractTestApplicationBefore3_3(variant: TestVariant): File {
    val output = variant.outputs.first()

    return File(
        when (output) {
            is ApkVariantOutput -> {
                variant.packageApplicationProvider
                File(variant.packageApplication.outputDirectory.asFile.get(), output.outputFileName).path
            }
            is LibraryVariantOutput -> {
                output.outputFile.path
            }
            else -> {
                throw RuntimeException("Can't find instrumentationApk")
            }
        }
    )
}

private fun extractTestApplication3_3_to_3_5(output: TestVariant): File {
    val testPackageAndroidArtifact = when (output) {
        is TestVariant -> {
            output.packageApplicationProvider
        }
        is LibraryVariant -> {
            output.packageLibraryProvider
        }
        else -> {
            throw RuntimeException("Can't find test application provider. Output is ${output.javaClass.canonicalName}")
        }
    }.get()


    return when (testPackageAndroidArtifact) {
        is PackageAndroidArtifact -> {
            assert(testPackageAndroidArtifact.variantOutputs.get().size == 1)
            File(
                testPackageAndroidArtifact.outputDirectory.asFile.get(),
                testPackageAndroidArtifact.variantOutputs.get().first().outputFileName.get()
            )
        }
        is Zip -> {
            testPackageAndroidArtifact.destFile
        }
        else -> throw GradleException("Unknown artifact package type")
    }
}
