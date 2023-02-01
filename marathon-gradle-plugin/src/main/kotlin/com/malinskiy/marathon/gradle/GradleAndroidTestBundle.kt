package com.malinskiy.marathon.gradle

import com.android.build.api.variant.BuiltArtifactsLoader
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.Optional
import java.io.Serializable

/**
 * Can't just make the properties nullable, see https://github.com/gradle/gradle/issues/2016
 */
sealed class GradleAndroidTestBundle : Serializable {
    class ApplicationWithTest(
        @InputDirectory val apkFolder: DirectoryProperty,
        @Internal val artifactLoader: Property<BuiltArtifactsLoader>,
        @InputDirectory val testApkFolder: DirectoryProperty,
        @Internal val testArtifactLoader: Property<BuiltArtifactsLoader>,
    ) : GradleAndroidTestBundle(), Serializable

    class TestOnly(
        @InputDirectory val testApkFolder: DirectoryProperty,
        @Internal val testArtifactLoader: Property<BuiltArtifactsLoader>,
    ) : GradleAndroidTestBundle(), Serializable
}
