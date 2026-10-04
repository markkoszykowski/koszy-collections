rootProject.name = "koszy-generator"

pluginManagement {
	includeBuild(rootDir.parentFile.resolve("build-logic").path)
}

plugins {
	id("com.koszy.settings")
}

includeBuild(rootDir.parentFile.resolve("util"))
