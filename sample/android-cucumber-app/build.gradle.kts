buildscript {
    repositories {
        mavenCentral()
        google()
    }
    dependencies {
        classpath(BuildPlugins.kotlinPlugin)
        classpath(BuildPlugins.androidGradle)
    }
}

allprojects {
    repositories {
        maven { url = uri("$rootDir/../build/repository") }
        mavenCentral()
        google()
        mavenLocal()
    }
}
