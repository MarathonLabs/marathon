package com.malinskiy.marathon.junit4

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.closureOf
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.findByType
import org.gradle.kotlin.dsl.findPlugin
import org.gradle.kotlin.dsl.getByName

class MarathonPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val extension: MarathonExtension = project.extensions.create("marathon", MarathonExtension::class.java, project)

        project.afterEvaluate {
            val javaBasePlugin =
                project.plugins.findPlugin(JavaBasePlugin::class) ?: throw IllegalStateException("Java plugin is not found")

            val marathonTask: Task = project.task(TASK_PREFIX, closureOf<Task> {
                group = JavaBasePlugin.VERIFICATION_GROUP
                description = "Runs all the test tasks"
            })

            val javaExtension =
                extensions.findByType(JavaPluginExtension::class) ?: throw IllegalStateException("No JavaPluginExtension is found")

            val conf = extensions.getByName("marathon") as? MarathonExtension ?: MarathonExtension(project)

            val sourceSetContainer =
                project.extensions.findByType<SourceSetContainer>() ?: throw IllegalStateException("No SourceSetContainer is found")

            val mainSourceSet =
                sourceSetContainer.findByName(SourceSet.MAIN_SOURCE_SET_NAME) ?: throw IllegalStateException("Sourceset main not found")

            sourceSetContainer.forEach {
                when (it.name) {
                    SourceSet.MAIN_SOURCE_SET_NAME -> Unit
                    SourceSet.TEST_SOURCE_SET_NAME -> {
                        val baseTestTask = createTask(project, conf, mainSourceSet, it)
                        val testTaskDependencies = project.tasks.getByName("test", Test::class).dependsOn
                        baseTestTask.dependsOn(testTaskDependencies)
                        marathonTask.dependsOn(baseTestTask)
                    }
                    else -> {
                        logger.warn("Unknown source set ${it.name}")
                    }
                }
            }
        }
    }

    companion object {
        private fun createTask(
            project: Project,
            config: MarathonExtension,
            mainSourceSet: SourceSet,
            testSourceSet: SourceSet
        ): MarathonRunTask {
            val marathonTask = project.tasks.create("${TASK_PREFIX}${testSourceSet.name.capitalize()}", MarathonRunTask::class)

            marathonTask.configure(closureOf<MarathonRunTask> {
                group = JavaBasePlugin.VERIFICATION_GROUP
                description = "Runs tests on all the connected devices for '${testSourceSet.name}' " +
                    "variation and generates a report"
                this.mainSourceSet.set(mainSourceSet.name)
                this.testSourceSet.set(testSourceSet.name)
                outputs.upToDateWhen { false }

                project.tasks.getByName(testSourceSet.name, Test::class)
                dependsOn()
            })

            return marathonTask
        }

        /**
         * Task name prefix.
         */
        private const val TASK_PREFIX = "marathon"
    }
}
