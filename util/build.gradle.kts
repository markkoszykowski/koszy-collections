plugins {
	id("com.koszy.project")
}

dependencies {
	api(libs.jspecify)
}

tasks.compileJava {
	options.release = 8
}
