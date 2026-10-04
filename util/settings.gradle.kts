rootProject.name = "koszy-util"

pluginManagement {
	includeBuild(rootDir.parentFile.resolve("build-logic").path)
}

plugins {
	id("com.koszy.settings")
}
