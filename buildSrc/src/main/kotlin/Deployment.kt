import org.gradle.api.Project
import org.gradle.api.publish.maven.MavenPom
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
            else -> "-SNAPSHOT"
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

    fun customizePom(pom: MavenPom?) {
        pom?.withXml {
            val root = asNode()
            root.appendNode("description", "Android & iOS test runner")
        }
    }
}
