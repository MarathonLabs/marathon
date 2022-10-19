package com.malinskiy.marathon.gradle

import com.android.build.api.artifact.SingleArtifact
import com.android.build.api.variant.AndroidTest
import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.android.build.api.variant.BuiltArtifactsLoader
import com.android.build.api.variant.LibraryAndroidComponentsExtension
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.Directory
import org.gradle.api.logging.Logger
import org.gradle.api.plugins.BasePlugin
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.closureOf
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.getByName

class MarathonPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val logger = project.logger
        logger.info("Applying marathon plugin")
        project.extensions.create("marathon", MarathonExtension::class.java, project)

        val rootProject = project.rootProject
        if (rootProject.extensions.findByName("marathon") == null) {
            applyRoot(rootProject)
        }
        val unpackMarathonTask = rootProject.tasks.getByName(MarathonUnpackTask.NAME, MarathonUnpackTask::class)
        val marathonCleanTask = rootProject.tasks[MarathonCleanTask.NAME]

        val appPlugin = project.plugins.findPlugin(AppPlugin::class.java)
        val libraryPlugin = project.plugins.findPlugin(LibraryPlugin::class.java)

        if (appPlugin == null && libraryPlugin == null) {
            throw IllegalStateException("Android plugin is not found")
        }

        val marathonTask: Task = project.task(TASK_PREFIX, closureOf<Task> {
            group = JavaBasePlugin.VERIFICATION_GROUP
            description = "Runs all the instrumentation test variations on all the connected devices"
        })

        val appExtension = project.extensions.findByType(ApplicationAndroidComponentsExtension::class.java)
        val libraryExtension = project.extensions.findByType(LibraryAndroidComponentsExtension::class.java)
        val conf = project.extensions.getByName("marathon") as? MarathonExtension ?: MarathonExtension(project)

        when {
            appExtension != null -> {
                val sdkDirectory: Provider<Directory> = appExtension.sdkComponents.sdkDirectory
                appExtension.onVariants { applicationVariant ->
                    val androidTest = applicationVariant.androidTest
                    if (androidTest != null) {
                        logger.info("Applying marathon for ${applicationVariant.name}")

                        val apkFolder: Provider<Directory> = applicationVariant.artifacts.get(SingleArtifact.APK)
                        val artifactsLoader = applicationVariant.artifacts.getBuiltArtifactsLoader()

                        val testApkFolder: Provider<Directory> = androidTest.artifacts.get(SingleArtifact.APK)
                        val testArtifactsLoader = androidTest.artifacts.getBuiltArtifactsLoader()

                        val bundles = listOf(
                            GradleAndroidTestBundle(
                                apkFolder = project.objects.directoryProperty().apply { set(apkFolder) },
                                artifactLoader = project.objects.property(BuiltArtifactsLoader::class.java)
                                    .apply { set(artifactsLoader) },
                                testApkFolder = project.objects.directoryProperty().apply { set(testApkFolder) },
                                testArtifactLoader = project.objects.property(BuiltArtifactsLoader::class.java)
                                    .apply { set(testArtifactsLoader) },
                            )
                        )

                        val testTaskForVariant = createTask(
                            logger, androidTest, bundles, project, conf, sdkDirectory
                        )
                        testTaskForVariant.dependsOn(unpackMarathonTask, apkFolder, testApkFolder)
                        marathonTask.dependsOn(testTaskForVariant)
                    }
                }
            }

            libraryExtension != null -> {
                val sdkDirectory: Provider<Directory> = libraryExtension.sdkComponents.sdkDirectory
                libraryExtension.onVariants { libraryVariant ->
                    val androidTest = libraryVariant.androidTest
                    if (androidTest != null) {
                        logger.info("Applying marathon for ${libraryVariant.name}")

                        val testApkFolder: Provider<Directory> = androidTest.artifacts.get(SingleArtifact.APK)
                        val testArtifactsLoader = androidTest.artifacts.getBuiltArtifactsLoader()

                        val bundles = listOf(
                            GradleAndroidTestBundle(
                                testApkFolder = project.objects.directoryProperty().apply { set(testApkFolder) },
                                testArtifactLoader = project.objects.property(BuiltArtifactsLoader::class.java)
                                    .apply { set(testArtifactsLoader) },
                            )
                        )

                        val testTaskForVariant = createTask(
                            logger, androidTest, bundles, project, conf, sdkDirectory
                        )
                        testTaskForVariant.dependsOn(unpackMarathonTask, testApkFolder)
                        marathonTask.dependsOn(testTaskForVariant)
                    }
                }
            }
            
            else -> throw IllegalStateException("No AndroidComponentsExtensions found. Did you apply marathon plugin after applying the application/library plugin?")
        }

        project.tasks[BasePlugin.CLEAN_TASK_NAME].dependsOn(marathonCleanTask)
    }

    private fun applyRoot(rootProject: Project) {
        rootProject.extensions.create("marathon", MarathonExtension::class.java, rootProject)
        rootProject.tasks.create(MarathonUnpackTask.NAME, MarathonUnpackTask::class.java)
        rootProject.tasks.create(MarathonCleanTask.NAME, MarathonCleanTask::class.java)
    }

    companion object {
        private fun createTask(
            logger: Logger,
            variant: AndroidTest,
            bundles: List<GradleAndroidTestBundle>,
            project: Project,
            config: MarathonExtension,
            sdkDirectory: Provider<Directory>,
        ): MarathonRunTask {
            val marathonTask = project.tasks.create("$TASK_PREFIX${variant.name.capitalize()}", MarathonRunTask::class.java)
            marathonTask.configure(closureOf<MarathonRunTask> {
                group = JavaBasePlugin.VERIFICATION_GROUP
                description = "Runs instrumentation tests on all the connected devices for '${variant.name}' " +
                    "variation and generates a report with screenshots"
                flavorName.set(variant.name)
                applicationBundles.set(bundles)
                marathonExtension.set(config)
                sdk.set(sdkDirectory)
                outputs.upToDateWhen { false }
            })

            return marathonTask
        }

        /**
         * Task name prefix.
         */
        private const val TASK_PREFIX = "marathon"
    }
}
