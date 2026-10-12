plugins {
	id("com.koszy.project")
}

java {
	toolchain {
		languageVersion.set(JavaLanguageVersion.of(8))
	}
}

dependencies {
	api(libs.jspecify)
}

tasks.withType<JavaCompile> {
	val addExports = javaCompiler.map { it.metadata }
		.map { it.languageVersion }
		.filter { it.canCompileOrRun(9) }
		.map { listOf("--add-exports=java.base/jdk.internal.misc=ALL-UNNAMED") }
	options.compilerArgumentProviders.add(CommandLineArgumentProvider { addExports.getOrElse(listOf()) })
}

tasks.withType<Test> {
	val addExports = javaLauncher.map { it.metadata }
		.map { it.languageVersion }
		.filter { it.canCompileOrRun(9) }
		.map { listOf("--add-exports=java.base/jdk.internal.misc=ALL-UNNAMED") }
	jvmArgumentProviders.add(CommandLineArgumentProvider { addExports.getOrElse(listOf()) })
}
