import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig.Mode

plugins {
    kotlin("js")
}

kotlin {
    js() {
        binaries.executable()
        browser {
            commonWebpackConfig {
                mode = if (project.hasProperty("prod")) Mode.PRODUCTION else Mode.DEVELOPMENT
            }
        }
        useCommonJs()
    }
}

val wrapperVersion = "17.0.2-pre.298-kotlin-1.6.10"
dependencies {
    implementation("org.jetbrains.kotlin-wrappers:kotlin-react-dom:$wrapperVersion")
    implementation("org.jetbrains.kotlin-wrappers:kotlin-react:$wrapperVersion")
//    implementation("org.jetbrains.kotlin-wrappers:kotlin-redux:4.1.2-pre.290-kotlin-1.6.10")
//    implementation("org.jetbrains.kotlin-wrappers:kotlin-react-redux:7.2.6-pre.290-kotlin-1.6.10")
//    implementation("org.jetbrains.kotlin-wrappers:kotlin-styled:5.3.3-pre.290-kotlin-1.6.10")
//    implementation("org.jetbrains.kotlin-wrappers:kotlin-react-router-dom:6.2.1-pre.290-kotlin-1.6.10")
}
