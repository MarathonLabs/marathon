package com.malinskiy.marathon.gradle.task

import com.android.build.api.variant.BuiltArtifacts
import com.malinskiy.marathon.config.Configuration
import com.malinskiy.marathon.config.serialization.ConfigurationFactory
import com.malinskiy.marathon.config.vendor.VendorConfiguration
import com.malinskiy.marathon.config.vendor.android.AndroidTestBundleConfiguration
import com.malinskiy.marathon.gradle.GradleAndroidTestBundle
import com.malinskiy.marathon.gradle.Const
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.listProperty
import org.gradle.kotlin.dsl.property
import java.io.File
import javax.inject.Inject

open class GenerateMarathonfileTask @Inject constructor(objects: ObjectFactory) : DefaultTask() {
    init {
        group = Const.GROUP
    }

    @Input
    val flavorName: Property<String> = objects.property()

    @Input
    val configurationBuilder: Property<Configuration.Builder> = objects.property()

    @Input
    val vendorConfigurationBuilder: Property<VendorConfiguration.AndroidConfigurationBuilder> = objects.property()

    // Overrides for the configuration from the AGP
    @Nested
    val applicationBundle: ListProperty<GradleAndroidTestBundle> = objects.listProperty()

    @InputDirectory
    @PathSensitive(PathSensitivity.NAME_ONLY)
    val sdk: DirectoryProperty = objects.directoryProperty()

    @OutputFile
    val marathonfile: RegularFileProperty = objects.fileProperty()

    @TaskAction
    fun write() {
        // Override stuff coming from AGP
        val androidConfiguration = vendorConfigurationBuilder.get().apply {
            androidSdk = sdk.get().asFile
            outputs = mapAndroidOutputs(applicationBundle.get(), flavorName.get())
        }.build()
        val cnf = configurationBuilder.get().apply {
            vendorConfiguration = androidConfiguration
        }.build()

        // Write a Marathonfile
        val configurationFactory = ConfigurationFactory(marathonfileDir = temporaryDir)
        val yaml = configurationFactory.serialize(cnf)
        marathonfile.get().asFile.writeText(yaml)
    }

    private fun mapAndroidOutputs(
        bundles: List<GradleAndroidTestBundle>,
        flavorName: String
    ): List<AndroidTestBundleConfiguration> {
        val outputs = bundles.map {
            val application = if (it.artifactLoader != null && it.apkFolder != null) {
                val artifactLoader = it.artifactLoader.get()
                val artifacts: BuiltArtifacts =
                    artifactLoader.load(it.apkFolder.get()) ?: throw RuntimeException("No application artifact found")
                when {
                    artifacts.elements.size > 1 -> throw UnsupportedOperationException(
                        "The Marathon plugin does not support abi splits for app APKs, " +
                            "but supports testing via a universal APK. "
                            + "Add the flag \"universalApk true\" in the android.splits.abi configuration."
                    )

                    artifacts.elements.isEmpty() -> throw UnsupportedOperationException("No artifacts for variant $flavorName")
                }
                File(artifacts.elements.first().outputFile)
            } else null

            val testArtifactsLoader = it.testArtifactLoader.get()
            val testArtifacts =
                testArtifactsLoader.load(it.testApkFolder.get()) ?: throw RuntimeException("No test artifacts for variant $flavorName")
            when {
                testArtifacts.elements.size > 1 -> throw UnsupportedOperationException("The Marathon plugin does not support abi/density splits for test APKs")
                testArtifacts.elements.isEmpty() -> throw UnsupportedOperationException("No test artifacts for variant $flavorName")
            }
            val testApplication = File(testArtifacts.elements.first().outputFile)

            AndroidTestBundleConfiguration(
                application = application,
                testApplication = testApplication,
                extraApplications = emptyList(),
            )
        }
        return outputs
    }
}
