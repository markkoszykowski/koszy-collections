import com.koszy.gradle.KoszyGradleUtil

plugins {
	id("com.koszy.project")
}

java {
	toolchain {
		languageVersion.set(JavaLanguageVersion.of(8))
	}
}

dependencies {
	implementation(KoszyGradleUtil.includedBuild(project, ":util"))

	implementation(libs.fastutil)
	implementation(libs.slf4j.api)
	implementation(libs.logback.classic)
}
