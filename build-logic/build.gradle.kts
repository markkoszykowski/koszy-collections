import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

plugins {
	id("java-gradle-plugin")
	id("idea")
	id("checkstyle")
	alias(libs.plugins.git.properties)
	alias(libs.plugins.versions)
}

fun languageVersion(): JavaLanguageVersion {
	val languageVersion = JavaLanguageVersion.current()
	if (!languageVersion.canCompileOrRun(JavaLanguageVersion.of(17))) {
		throw GradleException("Incompatible language version")
	}
	return languageVersion
}

java {
	toolchain {
		languageVersion = languageVersion()
	}
}

idea {
	module {
		isDownloadJavadoc = true
		isDownloadSources = true
	}
}

checkstyle {
	toolVersion = libs.versions.checkstyle.get()
}

repositories {
	mavenLocal()
	mavenCentral()
	gradlePluginPortal()
}

dependencies {
	api(libs.dependency.versions)
	api(libs.git.properties)
}

gradlePlugin {
	plugins {
		register("koszy-settings") {
			id = "com.koszy.settings"
			implementationClass = "com.koszy.gradle.KoszySettingsPlugin"
		}
		register("koszy-project") {
			id = "com.koszy.project"
			implementationClass = "com.koszy.gradle.KoszyProjectPlugin"
		}
	}
}

// https://github.com/ben-manes/gradle-versions-plugin

fun String.isNonStable(): Boolean {
	val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { uppercase().contains(it) }
	val regex = "^[0-9,.v-]+(-r|-jre|-android)?$".toRegex()
	val isStable = stableKeyword || regex.matches(this)
	return isStable.not()
}

tasks.withType<DependencyUpdatesTask> {
	resolutionStrategy {
		componentSelection {
			all {
				if (candidate.version.isNonStable() && !currentVersion.isNonStable()) {
					reject("Release candidate")
				}
			}
		}
	}
}
