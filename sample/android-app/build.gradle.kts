buildscript {
    repositories {
        jcenter()
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
        jcenter()
        mavenCentral()
        google()
        mavenLocal()
    }
}
