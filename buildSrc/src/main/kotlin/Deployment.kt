import org.gradle.api.Project
import org.gradle.api.publish.maven.MavenPom
import org.gradle.kotlin.dsl.extra
import org.gradle.kotlin.dsl.*

object Deployment {
    val user = System.getenv("SONATYPE_USERNAME")
    val password = System.getenv("SONATYPE_PASSWORD")
    var releaseMode: String? = null
    var versionSuffix: String? = null
    var deployUrl: String? = null

    val snapshotDeployUrl = "https://oss.sonatype.org/content/repositories/snapshots/"
    val releaseDeployUrl = "https://oss.sonatype.org/service/local/staging/deploy/maven2/"

    fun initialize(project: Project) {
        val releaseMode: String? by project
        val versionSuffix = when (releaseMode) {
            "RELEASE" -> ""
//            else -> "-SNAPSHOT" //TODO: revert
            else -> ""
        }

        Deployment.releaseMode = releaseMode
        Deployment.versionSuffix = versionSuffix
        Deployment.deployUrl = when (releaseMode) {
            "RELEASE" -> Deployment.releaseDeployUrl
            else -> Deployment.snapshotDeployUrl
        }

        project.extra.set("signing.keyId", "1131CBA5")
        project.extra.set("signing.password", System.getenv("GPG_PASSPHRASE"))
        project.extra.set("signing.secretKeyRingFile", "${project.rootProject.rootDir}/.buildsystem/secring.gpg")
    }

    fun customizePom(project: Project, pom: MavenPom?) {
        pom?.apply {
            name.set(project.name)
            url.set("https://github.com/Malinskiy/marathon")
            description.set("Android & iOS test runner")

            licenses {
                license {
                    name.set("The Apache License, Version 2.0")
                    url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                }
            }

            developers {
                developer {
                    id.set("marathon-team")
                    name.set("Marathon team")
                    email.set("anton@malinskiy.com")
                }
            }

            scm {
                url.set("https://github.com/Malinskiy/marathon")
            }
        }
    }
}
