package com.malinskiy.marathon.buildsystem

import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.getByName
import org.gradle.kotlin.dsl.register
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.kotlin.dsl.property
import org.gradle.plugins.ide.idea.model.IdeaModel
import java.io.File
import javax.inject.Inject


open class XcresulttoolPluginExtension {
    lateinit var pkg: String 
}

class XcresulttoolPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val extension = project.extensions.create<XcresulttoolPluginExtension>("xcresulttool")
        val sourceSets = project.extensions.getByType(SourceSetContainer::class.java)

        val mainSourceSet = sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME)
        val schemaFile = mainSourceSet.allSource.find { it.name == "xcresulttool-schema.json" }
        if (schemaFile == null) {
            project.logger.warn("No xcresulttool schema file found. Skipping codegen")
        } else {
            val output = project.file("${project.buildDir}/generated/source/xcresulttool/main/kotlin")
            val generateTask = project.tasks.register<GenerateTask>("generateXcresulttoolSource") {
                schema.set(schemaFile)
                pkg.set("com.malinskiy.marathon.vendor.ios.xcrun.xcresulttool")
                generatedDirectory.set(output)
            }
            project.tasks.getByName<KotlinCompile>("compileKotlin").dependsOn(generateTask)
            mainSourceSet.java.srcDir(output)

            val idea = project.getExtensions().findByType(IdeaModel::class.java)
            idea?.module?.generatedSourceDirs?.add(output.apply { mkdirs() })
        }
    }
}

open class GenerateTask @Inject constructor(objects: ObjectFactory) : DefaultTask() {

    @InputFile
    val schema: RegularFileProperty = objects.fileProperty()

    @Input
    val pkg: Property<String> = objects.property()
    
    @OutputDirectory
    val generatedDirectory: DirectoryProperty = objects.directoryProperty()

    @TaskAction
    fun action() {
        val output = generatedDirectory.get().asFile
        output.deleteRecursively()

        val configCreator = XcresulttoolSourceCreator(schema.get().asFile, pkg.get())
        configCreator.createKotlinSource(output)
    }
}
