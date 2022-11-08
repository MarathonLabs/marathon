package com.malinskiy.marathon.gradle

import com.android.build.api.variant.BuiltArtifactsLoader
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Nested
import java.io.Serializable

data class GradleAndroidTestBundle(
    @InputDirectory val apkFolder: DirectoryProperty? = null,
    @Internal val artifactLoader: Property<BuiltArtifactsLoader>? = null,
    @InputDirectory val testApkFolder: DirectoryProperty,
    @Internal val testArtifactLoader: Property<BuiltArtifactsLoader>,
) : Serializable
