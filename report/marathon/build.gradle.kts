import org.jetbrains.kotlin.gradle.plugin.KotlinJsCompilerType
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig.Mode

plugins {
    kotlin("js")
    kotlin("plugin.serialization") version Versions.kotlin
}

kotlin {
    js(KotlinJsCompilerType.IR) {
        binaries.executable()
        browser {
            commonWebpackConfig {
                mode = if (project.hasProperty("prod")) Mode.PRODUCTION else Mode.DEVELOPMENT
            }
        }
        useCommonJs()
    }
}

val react = "17.0.2"
val wrapperVersion = "pre.290-kotlin-1.6.10"
val reactDom = "6.2.1"
dependencies {
    implementation("org.jetbrains.kotlin-wrappers:kotlin-react-dom:$react-$wrapperVersion")
    implementation("org.jetbrains.kotlin-wrappers:kotlin-react:$react-$wrapperVersion")
    implementation("org.jetbrains.kotlin-wrappers:kotlin-react-router-dom:$reactDom-$wrapperVersion")
    implementation("org.jetbrains.kotlin-wrappers:kotlin-redux:4.1.2-$wrapperVersion")
    implementation("org.jetbrains.kotlin-wrappers:kotlin-react-redux:7.2.6-$wrapperVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2")
//    implementation("org.jetbrains.kotlin-wrappers:kotlin-styled:5.3.3-pre.290-kotlin-1.6.10")
}
