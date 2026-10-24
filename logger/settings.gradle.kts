rootProject.name = "koszy-logger"

pluginManagement {
	includeBuild(rootDir.parentFile.resolve("build-logic").path)
}

plugins {
	id("com.koszy.settings")
}

includeBuild(rootDir.parentFile.resolve("util"))
