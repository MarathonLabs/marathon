import com.sun.org.apache.xalan.internal.Version
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.gradle.dsl.Coroutines
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.publish.maven.MavenPom
import org.gradle.internal.impldep.org.fusesource.jansi.internal.WindowsSupport
import org.junit.platform.gradle.plugin.FiltersExtension
import org.junit.platform.gradle.plugin.EnginesExtension
import org.junit.platform.gradle.plugin.JUnitPlatformExtension

plugins {
    `java-library`
    id("org.jetbrains.kotlin.jvm")
    id("org.junit.platform.gradle.plugin")
    `maven-publish`
    `signing`
}

kotlin.experimental.coroutines = Coroutines.ENABLE

dependencies {
    implementation(project(":marathon-html-report"))
    implementation(project(":execution-timeline"))
    implementation(Libraries.gson)
    implementation(Libraries.apacheCommonsText)
    implementation(Libraries.apacheCommonsIO)
    implementation(Libraries.kotlinStdLib)
    implementation(Libraries.kotlinCoroutines)
    implementation(Libraries.kotlinLogging)
    implementation(Libraries.influxDbClient)
    testCompile(TestLibraries.kluent)
    testCompile(TestLibraries.spekAPI)
    testRuntime(TestLibraries.spekJUnitPlatformEngine)
}

val sourcesJar by tasks.creating(Jar::class) {
    classifier = "sources"
    from(java.sourceSets["main"].allSource)
}

val javadocJar by tasks.creating(Jar::class) {
    classifier = "javadoc"
    from(java.docsDir)
    dependsOn("javadoc")
}


publishing {
    publications {
        create("default", MavenPublication::class.java) {
            Deployment.customizePom(project, pom)
            from(components["java"])
            artifact(sourcesJar)
            artifact(javadocJar)
        }
    }
    repositories {
        maven {
            name = "Local"
            setUrl("$rootDir/build/repository")
        }
        maven {
            name = "OSSHR"
            credentials {
                username = Deployment.user
                password = Deployment.password
            }
            setUrl(Deployment.deployUrl)
        }
    }
}

signing {
    sign(publishing.publications.getByName("default"))
}

val compileKotlin by tasks.getting(KotlinCompile::class) {
    kotlinOptions.jvmTarget = "1.8"
}
val compileTestKotlin by tasks.getting(KotlinCompile::class) {
    kotlinOptions.jvmTarget = "1.8"
}

junitPlatform {
    filters {
        engines {
            include("spek")
        }
    }
}

// extension for configuration
fun JUnitPlatformExtension.filters(setup: FiltersExtension.() -> Unit) {
    when (this) {
        is ExtensionAware -> extensions.getByType(FiltersExtension::class.java).setup()
        else -> throw IllegalArgumentException("${this::class} must be an instance of ExtensionAware")
    }
}

fun FiltersExtension.engines(setup: EnginesExtension.() -> Unit) {
    when (this) {
        is ExtensionAware -> extensions.getByType(EnginesExtension::class.java).setup()
        else -> throw IllegalArgumentException("${this::class} must be an instance of ExtensionAware")
    }
}
