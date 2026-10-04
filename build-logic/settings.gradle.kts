rootProject.name = "build-logic"

dependencyResolutionManagement {
	versionCatalogs {
		create("libs") {
			from(files(rootDir.parentFile.resolve("gradle").resolve("libs.versions.toml")))
		}
	}
}
