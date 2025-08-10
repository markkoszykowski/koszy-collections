import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import com.github.benmanes.gradle.versions.updates.resolutionstrategy.ComponentSelectionWithCurrent

plugins {
	id("java")
	id("idea")
	id("checkstyle")
	alias(libs.plugins.versions)
}

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
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
	config
}

repositories {
	mavenLocal()
	mavenCentral()
}

dependencies {
	implementation(libs.fastutil)

	testImplementation(libs.agrona)

	testImplementation(libs.junit)
	testRuntimeOnly(libs.junit.launcher)

	testAnnotationProcessor(libs.jmh.annotations)
	testImplementation(libs.jmh.core)
}

tasks.test {
	useJUnitPlatform()
}

// https://github.com/ben-manes/gradle-versions-plugin

fun isNonStable(version: String): Boolean {
	val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.uppercase().contains(it) }
	val regex = "^[0-9,.v-]+(-r)?$".toRegex()
	val isStable = stableKeyword || regex.matches(version)
	return isStable.not()
}

class SelectionRules : Action<ComponentSelectionWithCurrent> {
	override fun execute(selection: ComponentSelectionWithCurrent) {
		if (isNonStable(selection.candidate.version) && !isNonStable(selection.currentVersion)) {
			selection.reject("Release candidate")
		}
	}

}

tasks.withType<DependencyUpdatesTask> {
	resolutionStrategy {
		componentSelection {
			all(SelectionRules())
		}
	}
}
