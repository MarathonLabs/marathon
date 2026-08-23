// Wire the root version catalog into buildSrc so `libs.foo` references
// resolve the same way here as in the main build. Without this, buildSrc has
// no `libs` accessor and dep versions drift silently from the catalog.
dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}
