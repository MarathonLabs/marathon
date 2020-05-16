import com.google.protobuf.gradle.*
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `java-library`
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.dokka")
    id("org.junit.platform.gradle.plugin")
    id("com.google.protobuf") version "0.8.8"
    id("idea")
}


repositories {
    mavenLocal()
    maven("https://plugins.gradle.org/m2/")
}



dependencies {
    implementation(Libraries.kotlinStdLib)
    implementation(Libraries.kotlinCoroutines)
    implementation(Libraries.kotlinLogging)
    implementation(Libraries.kotlinReflect)
    implementation(Libraries.slf4jAPI)
    implementation(Libraries.logbackClassic)
    implementation(Libraries.ddPlist)
    implementation(Libraries.guava)
    implementation(Libraries.rsync4j)
    implementation(Libraries.sshj)
    implementation(Libraries.gson)
    implementation(Libraries.jacksonKotlin)
    implementation(Libraries.jacksonYaml)
    implementation(Libraries.jansi)
    implementation(project(":core"))
    implementation(Libraries.apacheCommonsText)
    testImplementation(TestLibraries.kluent)
    testImplementation(TestLibraries.mockitoKotlin)
    testImplementation(TestLibraries.testContainers)
    testImplementation(TestLibraries.junit5)
    testRuntime(TestLibraries.jupiterEngine)


    implementation("io.grpc:grpc-kotlin-stub:0.1.2")
    implementation("com.google.protobuf:protobuf-java:3.11.1")
    implementation("com.google.protobuf:protobuf-java-util:3.11.1")
    implementation("io.grpc:grpc-netty-shaded:1.28.1")
    implementation("io.grpc:grpc-protobuf:1.28.1")
    implementation("io.grpc:grpc-stub:1.28.1")
    compileOnly("javax.annotation:javax.annotation-api:1.2")
    implementation("com.google.guava:guava:28.2-jre")
}

protobuf {
    generatedFilesBaseDir = "src/"
    protoc { artifact = "com.google.protobuf:protoc:3.11.1" }
    plugins {
        id("grpc") { artifact = "io.grpc:protoc-gen-grpc-java:1.15.1" }
        id("grpckt") { artifact = "io.grpc:protoc-gen-grpc-kotlin:0.1.2" }
    }
    generateProtoTasks {
        ofSourceSet("main").forEach {
            it.plugins {
                id("grpc") { outputSubDir = "java"}
                id("grpckt") { outputSubDir = "kotlin"}
            }
        }
    }
}




Deployment.initialize(project)

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
    kotlinOptions.apiVersion = "1.3"
}

tasks.withType<Test>().all {
    tasks.getByName("check").dependsOn(this)
    useJUnitPlatform()
}

junitPlatform {
    enableStandardTestTask = true
}

tasks.getByName("junitPlatformTest").outputs.upToDateWhen { false }
tasks.getByName("test").outputs.upToDateWhen { false }
