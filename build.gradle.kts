buildscript {
    repositories {
        jcenter()
        mavenCentral()
        google()
    }
    dependencies {
        classpath(BuildPlugins.kotlinPlugin)
        classpath(BuildPlugins.junitGradle)
        classpath(BuildPlugins.androidGradle)
    }
}

allprojects {
    group = "com.malinskiy"
    version = "0.1.0"

    repositories {
        jcenter()
        mavenCentral()
        google()
    }
}