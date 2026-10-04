package com.koszy.gradle;

import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask;
import com.gorylenko.GitPropertiesPlugin;
import org.gradle.api.GradleException;
import org.gradle.api.JavaVersion;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.MinimalExternalModuleDependency;
import org.gradle.api.artifacts.VersionConstraint;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.artifacts.dsl.RepositoryHandler;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.plugins.ExtensionContainer;
import org.gradle.api.plugins.JavaLibraryPlugin;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.plugins.PluginContainer;
import org.gradle.api.plugins.quality.CheckstyleExtension;
import org.gradle.api.plugins.quality.CheckstylePlugin;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.compile.CompileOptions;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.api.tasks.testing.Test;
import org.gradle.jvm.toolchain.JavaLanguageVersion;
import org.gradle.plugins.ide.idea.IdeaPlugin;
import org.gradle.plugins.ide.idea.model.IdeaModel;
import org.gradle.testing.jacoco.plugins.JacocoPlugin;
import org.gradle.testing.jacoco.plugins.JacocoPluginExtension;
import org.jspecify.annotations.NonNull;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@SuppressWarnings(value = {"unused"})
public final class KoszyProjectPlugin implements Plugin<Project> {

	private static final Logger logger = Logging.getLogger(KoszyProjectPlugin.class);

	public static final JavaLanguageVersion MINIMUM_JAVA_LANGUAGE_VERSION = JavaLanguageVersion.of(JavaVersion.VERSION_17.getMajorVersion());

	public static final String VERSION = "VERSION.txt";

	public static final String CHECKSTYLE = "checkstyle";
	public static final String JACOCO = "jacoco";

	public static final String JUNIT = "junit";
	public static final String JUNIT_LAUNCHER = "junit.launcher";

	public static final String MOCKITO = "mockito";

	public static final String AWAITILITY = "awaitility";

	public static final String MOCKITO_AGENT = "mockitoAgent";
	private static final org.slf4j.Logger log = LoggerFactory.getLogger(KoszyProjectPlugin.class);

	public static Optional<String> version(final VersionConstraint constraint) {
		return Stream.ofNullable(constraint)
				.flatMap(version -> Stream.of(
								version.getRequiredVersion(),
								version.getPreferredVersion(),
								version.getStrictVersion()
						)
				)
				.filter(Predicate.not(KoszyGradleUtil::isBlank))
				.findFirst();
	}

	public void configure(final Project project) {
		KoszyGradleUtil.find(project.getRootDir(), VERSION)
				.map(KoszyGradleUtil.function(Files::readString))
				.map(String::strip)
				.ifPresent(project::setVersion);
	}

	public void configure(final Project project, final PluginContainer plugins) {
		plugins.apply(JavaLibraryPlugin.class);
		plugins.apply(IdeaPlugin.class);
		plugins.apply(CheckstylePlugin.class);
		plugins.apply(JacocoPlugin.class);
		plugins.apply(GitPropertiesPlugin.class);
	}

	public void configure(final Project project, final ExtensionContainer extensions) {
		extensions.configure(JavaPluginExtension.class, java -> {
			final JavaLanguageVersion languageVersion = JavaLanguageVersion.current();
			if (!languageVersion.canCompileOrRun(MINIMUM_JAVA_LANGUAGE_VERSION)) {
				throw new GradleException("Incompatible Java Language Version: " + languageVersion);
			}
			java.toolchain(toolchain -> toolchain.getLanguageVersion().convention(languageVersion));
		});

		extensions.configure(IdeaModel.class, idea -> {
			idea.module(module -> {
				module.setDownloadJavadoc(true);
				module.setDownloadSources(true);
			});
		});

		extensions.configure(CheckstyleExtension.class, checkstyle -> {
			KoszyGradleUtil.libs(project)
					.flatMap(libs -> libs.findVersion(CHECKSTYLE))
					.flatMap(KoszyProjectPlugin::version)
					.ifPresent(version -> {
						logger.debug("[{}] Setting {} tool version to {}", project, CHECKSTYLE, version);
						checkstyle.setToolVersion(version);
						KoszyGradleUtil.find(project.getRootDir(), "config")
								.map(config -> config.resolve("checkstyle"))
								.filter(Files::isDirectory)
								.map(Path::toFile)
								.map(KoszyGradleUtil::constant)
								.map(project::provider)
								.map(config -> project.getLayout().dir(config))
								.ifPresent(config -> checkstyle.getConfigDirectory().convention(config));
					});
		});

		extensions.configure(JacocoPluginExtension.class, jacoco -> {
			KoszyGradleUtil.libs(project)
					.flatMap(libs -> libs.findVersion(JACOCO))
					.flatMap(KoszyProjectPlugin::version)
					.ifPresent(version -> {
						logger.debug("[{}] Setting {} tool version to {}", project, JACOCO, version);
						jacoco.setToolVersion(version);
					});
		});
	}

	public void configure(final Project project, final RepositoryHandler repositories) {
		repositories.mavenLocal();
		repositories.mavenCentral();
		repositories.gradlePluginPortal();
	}

	public void configure(final Project project, final DependencyHandler dependencies) {
		final BiConsumer<String, Provider<MinimalExternalModuleDependency>> autowire = (configuration, library) -> {
			logger.debug("[{}] Autowiring {} to {}", project, library.getOrNull(), configuration);
			dependencies.add(configuration, library);
		};
		KoszyGradleUtil.libs(project).ifPresent(libs -> {
			libs.findLibrary(JUNIT)
					.ifPresent(library -> {
						autowire.accept(JavaPlugin.TEST_IMPLEMENTATION_CONFIGURATION_NAME, library);
					});
			libs.findLibrary(JUNIT_LAUNCHER)
					.ifPresent(library -> {
						autowire.accept(JavaPlugin.TEST_RUNTIME_ONLY_CONFIGURATION_NAME, library);
					});

			libs.findLibrary(MOCKITO)
					.ifPresent(library -> {
						autowire.accept(JavaPlugin.TEST_IMPLEMENTATION_CONFIGURATION_NAME, library);

						final ConfigurationContainer configurations = project.getConfigurations();
						final Configuration configuration = configurations.create(MOCKITO_AGENT);
						configuration.setTransitive(false);
						dependencies.add(configuration.getName(), library);
					});

			libs.findLibrary(AWAITILITY)
					.ifPresent(library -> {
						autowire.accept(JavaPlugin.TEST_IMPLEMENTATION_CONFIGURATION_NAME, library);
					});
		});
	}

	public void configure(final Project project, final TaskContainer tasks) {
		tasks.withType(JavaCompile.class).configureEach(task -> {
			final CompileOptions options = task.getOptions();
			options.getCompilerArgs().add("-parameters");
			options.setEncoding(StandardCharsets.UTF_8.displayName());
		});

		tasks.withType(Test.class).configureEach(task -> {
			final ConfigurationContainer configurations = project.getConfigurations();
			Optional.ofNullable(configurations.findByName(MOCKITO_AGENT)).ifPresent(configuration -> {
				final Provider<List<String>> jvmArgs = project.provider(() -> List.of("-javaagent:" + configuration.getAsPath()));
				task.getJvmArgumentProviders().add(jvmArgs::get);
			});
		});
		tasks.named(JavaPlugin.TEST_TASK_NAME, Test.class).configure(task -> {
			task.useJUnitPlatform();
			task.finalizedBy("jacoco" + KoszyGradleUtil.capitalize(task.getName()) + "Report");
		});

		// https://github.com/ben-manes/gradle-versions-plugin
		final Predicate<String> isNonStable = version -> {
			final boolean stableKeyword = Stream.of("RELEASE", "FINAL", "GA").anyMatch(keyword -> version.toUpperCase().contains(keyword));
			final Predicate<String> regex = Pattern.compile("^[0-9,.v-]+(-r|-jre|-android)?$").asMatchPredicate();
			final boolean isStable = stableKeyword || regex.test(version);
			return !isStable;
		};

		tasks.withType(DependencyUpdatesTask.class).configureEach(task -> {
			task.setCheckForGradleUpdate(true);
			task.setRejectPreReleases(true);
			task.resolutionStrategy(resolutionStrategy -> {
				resolutionStrategy.componentSelection(componentSelection -> {
					componentSelection.all(selection -> {
						if (isNonStable.test(selection.getCandidate().getVersion()) && !isNonStable.test(selection.getCurrentVersion())) {
							selection.reject("Release candidate");
						}
					});
				});
			});
		});
	}

	@Override
	public void apply(final @NonNull Project target) {
		this.configure(target);
		this.configure(target, target.getPlugins());
		this.configure(target, target.getExtensions());
		this.configure(target, target.getRepositories());
		this.configure(target, target.getDependencies());
		this.configure(target, target.getTasks());
	}

}
