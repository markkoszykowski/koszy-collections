import com.koszy.gradle.KoszyGradleUtil

plugins {
	id("com.koszy.project")
}

dependencies {
	implementation(KoszyGradleUtil.includedBuild(project, ":util"))

	api(libs.javapoet)
	api(libs.jspecify)
}
