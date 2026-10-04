rootProject.name = "koszy-collections"

pluginManagement {
	includeBuild(rootDir.resolve("build-logic").path)
}

plugins {
	id("com.koszy.settings")
}

includeBuild("generator")
includeBuild("util")
