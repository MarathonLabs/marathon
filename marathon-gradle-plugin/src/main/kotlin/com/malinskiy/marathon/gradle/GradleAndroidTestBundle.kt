package com.malinskiy.marathon.gradle

import com.android.build.api.variant.BuiltArtifactsLoader
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property

data class GradleAndroidTestBundle(
    val apkFolder: DirectoryProperty? = null,
    val artifactLoader: Property<BuiltArtifactsLoader>? = null,
    val testApkFolder: DirectoryProperty,
    val testArtifactLoader: Property<BuiltArtifactsLoader>,
)
