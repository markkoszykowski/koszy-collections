import com.koszy.collections.generator.Generator
import com.koszy.collections.generator.OnceGenerator
import com.koszy.gradle.KoszyGradleUtil
import com.palantir.javapoet.JavaFile

plugins {
	id("com.koszy.project")
}

buildscript {
	dependencies {
		classpath(com.koszy.gradle.KoszyGradleUtil.includedBuild(project, ":generator"))
	}
}

dependencies {
	implementation(KoszyGradleUtil.includedBuild(project, ":util"))

	implementation(libs.fastutil)

	testAnnotationProcessor(libs.jmh.annotations)
	testImplementation(libs.jmh.core)
}

abstract class GenerateTask @Inject constructor(private val objects: ObjectFactory) : DefaultTask() {
	init {
		this.group = LifecycleBasePlugin.BUILD_GROUP
	}

	private val mainDirectory: Provider<Directory> = this.directory(this.project.sourceSets.main)
	private val testDirectory: Provider<Directory> = this.directory(this.project.sourceSets.test)

	@Input
	val generators: ListProperty<Generator> = this.objects.listProperty<Generator>().convention(listOf())

	private fun directory(sourceSet: NamedDomainObjectProvider<SourceSet>): Provider<Directory> {
		val directory = this.project.layout.buildDirectory.dir("generated")
			.map { dir -> dir.dir("sources") }
			.map { dir -> dir.dir("java") }
			.flatMap { dir -> sourceSet.map { set -> set.name }.map { name -> dir.dir(name) } }
		sourceSet.configure {
			this.java.srcDir(directory)
		}
		return directory
	}

	private fun generate(generators: List<Generator>, directory: Directory, generate: (Generator) -> List<JavaFile>) {
		generators.forEach { generator ->
			generate(generator).forEach { source ->
				source.writeTo(directory.asFile)
			}
		}
	}

	@TaskAction
	fun generate() {
		val generators = this.generators.get()
		this.generate(generators, this.mainDirectory.get()) { it.generateMainSources() }
		this.generate(generators, this.testDirectory.get()) { it.generateTestSources() }
	}
}

val generateSources = tasks.register<GenerateTask>("generateSources") {
	group = LifecycleBasePlugin.BUILD_GROUP

	generators.addAll(
		OnceGenerator()
	)
}

tasks.compileJava {
	dependsOn(generateSources)

	options.release = 8
}

tasks.compileTestJava {
	dependsOn(generateSources)

	options.release = 8
}
