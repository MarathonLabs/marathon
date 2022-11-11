import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.withType
import org.gradle.testing.jacoco.tasks.JacocoReport
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

fun Project.setupKotlinCompiler() {
    tasks.withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"
        kotlinOptions.apiVersion = "1.5"
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
}

fun Project.setupDeployment() {
    Deployment.initialize(this)
}
