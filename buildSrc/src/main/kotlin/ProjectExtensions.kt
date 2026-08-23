import com.malinskiy.marathon.buildsystem.libs
import com.malinskiy.marathon.buildsystem.library
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.testing.Test
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.withType
import org.gradle.testing.jacoco.tasks.JacocoReport
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

/**
 * Applies the shared Kotlin/JVM compilation target across every module.
 *
 * Gradle 9's dependency resolution reads `org.gradle.jvm.version` from the
 * `java` and `kotlin` extension toolchains; without those, module-to-module
 * resolution attributes leak the daemon's JDK version (currently 21) and
 * inter-project deps fail with `"compatible with JVM runtime version 21 or
 * newer"`. We set toolchain + language + JavaCompile target explicitly.
 */
fun Project.setupKotlinCompiler(jvmTarget: String = "21") {
    val jvmInt = jvmTarget.toInt()
    val version = JavaVersion.toVersion(jvmTarget)

    // Kotlin 2.x drops `apiVersion = "1.5"`; the default (matching the compiler
    // version) is fine. `kotlinOptions.jvmTarget` remains available on 2.x with
    // a soft-deprecation; migration to `compilerOptions` is cosmetic.
    tasks.withType<KotlinCompile> {
        kotlinOptions.jvmTarget = jvmTarget
    }
    tasks.withType<JavaCompile> {
        sourceCompatibility = jvmTarget
        targetCompatibility = jvmTarget
    }
    extensions.findByType(JavaPluginExtension::class.java)?.let { java ->
        java.sourceCompatibility = version
        java.targetCompatibility = version
        // Toolchain drives the JVM variant attribute; pin at the target so
        // downstream consumers on JDK 11 can still resolve this module.
        java.toolchain.languageVersion.set(JavaLanguageVersion.of(jvmInt))
    }
    extensions.findByType(KotlinJvmProjectExtension::class.java)?.let { kotlin ->
        kotlin.jvmToolchain(jvmInt)
    }
}

fun Project.setupTestTask(jacoco: Boolean = true) {
    if (jacoco) {
        tasks.named<JacocoReport>("jacocoTestReport").configure {
            reports.xml.required.set(true)
            reports.html.required.set(true)
            dependsOn(tasks.named("test"))
        }
    }

    tasks.withType<Test>().all {
        tasks.getByName("check").dependsOn(this)
        useJUnitPlatform()
    }

    // JUnit 5.8+ split the platform launcher out of `junit-jupiter`. Gradle 9's
    // test workers no longer resolve it transitively, so add it wherever we
    // wire the test infrastructure — matches what
    // `junit-platform-gradle-plugin` used to do. Coordinates come from the
    // root version catalog so upgrades touch one file (gradle/libs.versions.toml).
    configurations.findByName("testRuntimeOnly")?.let { cfg ->
        dependencies.add(cfg.name, libs.library("junitPlatformLauncher").get())
    }
}

fun Project.setupDeployment() {
    Deployment.initialize(this)
}
