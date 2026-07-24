import com.github.gradle.node.npm.task.NpmTask

plugins {
    `java-library`
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.dokka")
    id("com.github.node-gradle.node")
}

dependencies {
    implementation(libs.gson)
    implementation(libs.kotlinStdLib)
    implementation(libs.kotlinCoroutines)
    implementation(libs.kotlinLogging)
}

// HTML report bundle
//
// Frontend lives at repo root under `html-report/` (Vite + React 18 + TS).
// `npm run build` produces `dist/app.min.{js,css}` — the two files the Kotlin
// reporter streams into every emitted report directory.
//
// Node is provisioned by the node-gradle plugin (downloads Node 24 into
// `build/nodejs/`); no host-level Node install required. The compiled bundle
// lands in `build/generated-resources/html-report/html-report/` and is
// registered as a resource source dir, so `processResources` packages it into
// the module jar automatically. Nothing is committed to git.
val htmlReportSrcDir = rootProject.file("html-report")

node {
    version.set("24.4.0")
    npmVersion.set("")
    download.set(true)
    workDir.set(layout.buildDirectory.dir("nodejs"))
    npmWorkDir.set(layout.buildDirectory.dir("npm"))
    nodeProjectDir.set(htmlReportSrcDir)
}

val generatedResourcesDir = layout.buildDirectory.dir("generated-resources/html-report")
val bundleStagingDir = generatedResourcesDir.map { it.dir("html-report") }
val viteOutputDir = htmlReportSrcDir.resolve("dist")

val viteBuild = tasks.register<NpmTask>("viteBuild") {
    group = "html-report"
    description = "Build the html-report bundle via Vite (Node provisioned by gradle-node-plugin)."
    dependsOn(tasks.named("npmInstall"))
    args.set(listOf("run", "build"))
    inputs.dir(htmlReportSrcDir.resolve("src"))
        .withPathSensitivity(PathSensitivity.RELATIVE)
    inputs.file(htmlReportSrcDir.resolve("index.html"))
        .withPathSensitivity(PathSensitivity.RELATIVE)
    inputs.file(htmlReportSrcDir.resolve("vite.config.ts"))
        .withPathSensitivity(PathSensitivity.RELATIVE)
    inputs.file(htmlReportSrcDir.resolve("tsconfig.json"))
        .withPathSensitivity(PathSensitivity.RELATIVE)
    inputs.file(htmlReportSrcDir.resolve("tailwind.config.ts"))
        .withPathSensitivity(PathSensitivity.RELATIVE)
    inputs.file(htmlReportSrcDir.resolve("postcss.config.cjs"))
        .withPathSensitivity(PathSensitivity.RELATIVE)
    inputs.file(htmlReportSrcDir.resolve("package.json"))
        .withPathSensitivity(PathSensitivity.RELATIVE)
    outputs.dir(viteOutputDir)
}

val bundleHtmlReportApp = tasks.register<Copy>("bundleHtmlReportApp") {
    group = "html-report"
    description = "Stage compiled app bundle into build/generated-resources/."
    dependsOn(viteBuild)
    from(viteOutputDir) {
        include("app.min.js", "app.min.css")
    }
    into(bundleStagingDir)
}

tasks.register("bundleHtmlReport") {
    group = "html-report"
    description = "Rebuild the html-report bundle."
    dependsOn(bundleHtmlReportApp)
}

sourceSets["main"].resources.srcDir(generatedResourcesDir)

tasks.named("processResources").configure {
    dependsOn(bundleHtmlReportApp)
}

setupDeployment()
setupKotlinCompiler()
