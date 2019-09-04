package com.malinskiy.marathon.extensions

import com.android.build.gradle.api.ApkVariantOutput
import com.android.build.gradle.api.LibraryVariant
import com.android.build.gradle.api.LibraryVariantOutput
import com.android.build.gradle.api.TestVariant
import com.android.build.gradle.tasks.PackageAndroidArtifact
import org.apache.tools.ant.taskdefs.Zip
import org.gradle.api.GradleException
import java.io.File

fun TestVariant.extractTestApplication() = com.malinskiy.marathon.extensions.executeGradleCompat(
    exec = {
        extractTestApplication3_3_plus(this)
    },
    fallback = {
        extractTestApplicationBefore3_3(this)
    }
)

private fun extractTestApplicationBefore3_3(variant: TestVariant): File {
    val output = variant.outputs.first()

    return File(
        when (output) {
            is ApkVariantOutput -> {
                File(variant.packageApplication.outputDirectory.path, output.outputFileName).path
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

private fun extractTestApplication3_3_plus(output: TestVariant): File {
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
            assert(testPackageAndroidArtifact.apkNames.size == 1)
            File(testPackageAndroidArtifact.outputDirectory, testPackageAndroidArtifact.apkNames.first())
        }
        is Zip -> {
            testPackageAndroidArtifact.destFile
        }
        else -> throw GradleException("Unknown artifact package type")
    }
}