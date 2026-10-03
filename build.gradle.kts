import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import com.github.benmanes.gradle.versions.updates.resolutionstrategy.ComponentSelectionWithCurrent
import java.nio.charset.StandardCharsets

plugins {
	id("java")
	id("idea")
	id("checkstyle")
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
}

dependencies {
	implementation(libs.fastutil)

	testImplementation(libs.agrona)

	testImplementation(libs.junit)
	testRuntimeOnly(libs.junit.launcher)

	testAnnotationProcessor(libs.jmh.annotations)
	testImplementation(libs.jmh.core)
}

tasks.withType<JavaCompile> {
	options.compilerArgs.add("-parameters")
	options.encoding = StandardCharsets.UTF_8.name()
}

tasks.compileJava {
	options.release = 8
}

tasks.test {
	useJUnitPlatform()
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