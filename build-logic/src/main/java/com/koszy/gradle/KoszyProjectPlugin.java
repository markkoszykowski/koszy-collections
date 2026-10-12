package com.koszy.gradle;

import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask;
import com.gorylenko.GenerateGitPropertiesTask;
import com.gorylenko.GitPropertiesPlugin;
import org.gradle.api.DefaultTask;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.ConfigurationPublications;
import org.gradle.api.artifacts.ConfigurationVariant;
import org.gradle.api.artifacts.MinimalExternalModuleDependency;
import org.gradle.api.artifacts.VersionConstraint;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.artifacts.dsl.RepositoryHandler;
import org.gradle.api.attributes.LibraryElements;
import org.gradle.api.file.FileCollection;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.plugins.ExtensionContainer;
import org.gradle.api.plugins.HelpTasksPlugin;
import org.gradle.api.plugins.JavaLibraryPlugin;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.plugins.JvmTestSuitePlugin;
import org.gradle.api.plugins.PluginContainer;
import org.gradle.api.plugins.jvm.JvmTestSuite;
import org.gradle.api.plugins.jvm.JvmTestSuiteTarget;
import org.gradle.api.plugins.quality.Checkstyle;
import org.gradle.api.plugins.quality.CheckstyleExtension;
import org.gradle.api.plugins.quality.CheckstylePlugin;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskOutputs;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.bundling.Jar;
import org.gradle.api.tasks.compile.CompileOptions;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.api.tasks.testing.Test;
import org.gradle.api.tasks.testing.logging.TestLoggingContainer;
import org.gradle.internal.MutableBoolean;
import org.gradle.internal.Pair;
import org.gradle.jvm.toolchain.JavaLanguageVersion;
import org.gradle.jvm.toolchain.JavaToolchainService;
import org.gradle.language.base.plugins.LifecycleBasePlugin;
import org.gradle.plugins.ide.idea.IdeaPlugin;
import org.gradle.plugins.ide.idea.model.IdeaModel;
import org.gradle.testing.base.TestSuite;
import org.gradle.testing.base.TestingExtension;
import org.gradle.testing.jacoco.plugins.JacocoPlugin;
import org.gradle.testing.jacoco.plugins.JacocoPluginExtension;
import org.jspecify.annotations.NonNull;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.jar.Attributes;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@SuppressWarnings(value = {"unused"})
public final class KoszyProjectPlugin implements Plugin<Project> {

	private static final Logger logger = Logging.getLogger(KoszyProjectPlugin.class);

	public static final String VERSION = "VERSION.txt";

	public static final String CHECKSTYLE = "checkstyle";
	public static final String JACOCO = "jacoco";

	public static final String JUNIT = "junit";
	public static final String JUNIT_LAUNCHER = "junit.launcher";

	public static final String MOCKITO_CORE = "mockito.core";
	public static final String MOCKITO_INLINE = "mockito.inline";

	public static final String AWAITILITY = "awaitility";

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
		final ObjectFactory objects = project.getObjects();
		final JavaToolchainService javaToolchains = extensions.getByType(JavaToolchainService.class);

		extensions.configure(JavaPluginExtension.class, java -> {
			java.withSourcesJar();
		});

		extensions.configure(IdeaModel.class, idea -> {
			idea.module(module -> {
				module.setDownloadJavadoc(true);
				module.setDownloadSources(true);
			});
		});

		final MutableBoolean multiRelease = new MutableBoolean(false);
		extensions.configure(SourceSetContainer.class, sourceSets -> {
			final SourceSet mainSources = sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME);
			mainSources.getJava().getSrcDirs().stream()
					.findFirst()
					.stream()
					.map(File::toPath)
					.flatMap(KoszyGradleUtil.function(Files::list))
					.filter(Files::isDirectory)
					.map(folder -> Pair.of(folder, folder))
					.map(KoszyGradleUtil.rightFunction(Path::getFileName))
					.map(KoszyGradleUtil.rightFunction(Path::toString))
					.map(KoszyGradleUtil.rightFunction(KoszyGradleUtil::toInteger))
					.filter(KoszyGradleUtil.rightPredicate(Objects::nonNull))
					.sorted(Comparator.comparing(Pair::getRight))
					.peek(_ -> multiRelease.set(true))
					.forEach(release -> {
						final SourceSet set = sourceSets.create(SourceSet.MAIN_SOURCE_SET_NAME + release.right);
						set.getJava().srcDir(release.left);
						mainSources.getJava().exclude(Path.of(String.valueOf(release.right)).resolve("**").toString());

						project.getTasks().named(set.getCompileJavaTaskName(), JavaCompile.class).configure(compileJava -> {
							compileJava.getJavaCompiler().set(javaToolchains.compilerFor(toolchain -> {
								toolchain.getLanguageVersion().set(JavaLanguageVersion.of(release.right));
							}));
						});

						final Map<String, Object> configurations = Map.of(
								mainSources.getJarTaskName(), set.getOutput()
						);
						for (final Map.Entry<String, Object> configuration : configurations.entrySet()) {
							project.getTasks().named(configuration.getKey(), Jar.class).configure(task -> {
								task.from(configuration.getValue(), copy -> {
									copy.into(Path.of("META-INF").resolve("versions").resolve(String.valueOf(release.right)).toFile());
								});
							});
						}

						extensions.configure(TestingExtension.class, testing -> {
							final NamedDomainObjectContainer<TestSuite> suites = testing.getSuites();
							suites.named(JvmTestSuitePlugin.DEFAULT_TEST_SUITE_NAME, JvmTestSuite.class).configure(testSuite -> {
								final NamedDomainObjectContainer<? extends JvmTestSuiteTarget> targets = testSuite.getTargets();
								targets.register(JavaPlugin.TEST_TASK_NAME + release.right, target -> {
									target.getTestTask().configure(test -> {
										test.getJavaLauncher().set(javaToolchains.launcherFor(toolchain -> {
											toolchain.getLanguageVersion().set(JavaLanguageVersion.of(release.right));
										}));
									});
								});
							});
						});
					});
		});
		if (multiRelease.get()) {
			final ConfigurationContainer configurations = project.getConfigurations();
			final String[] configurationNames = {
					JavaPlugin.API_ELEMENTS_CONFIGURATION_NAME,
					JavaPlugin.RUNTIME_ELEMENTS_CONFIGURATION_NAME,
			};
			for (final String configurationName : configurationNames) {
				configurations.named(configurationName).configure(configuration -> {
					final ConfigurationPublications outputs = configuration.getOutgoing();
					final NamedDomainObjectContainer<ConfigurationVariant> variants = outputs.getVariants();
					final LibraryElements jar = objects.named(LibraryElements.class, LibraryElements.JAR);
					variants.removeIf(variant -> !Objects.equals(jar, variant.getAttributes().getAttribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE)));
				});
			}

			final TaskContainer tasks = project.getTasks();
			final TaskProvider<Jar> jar = tasks.named(JavaPlugin.JAR_TASK_NAME, Jar.class);
			jar.configure(task -> {
				task.manifest(manifest -> {
					manifest.attributes(Map.of(Attributes.Name.MULTI_RELEASE.toString(), true));
				});
			});

			extensions.configure(SourceSetContainer.class, sourceSets -> {
				final SourceSet mainSources = sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME);
				final SourceSet testSources = sourceSets.getByName(SourceSet.TEST_SOURCE_SET_NAME);
				final FileCollection outputs = objects.fileCollection().from(jar.map(Jar::getOutputs).map(TaskOutputs::getFiles));
				testSources.setCompileClasspath(testSources.getCompileClasspath().minus(mainSources.getOutput()).plus(outputs));
				testSources.setRuntimeClasspath(testSources.getRuntimeClasspath().minus(mainSources.getOutput()).plus(outputs));
			});
		}

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

			libs.findLibrary(MOCKITO_CORE)
					.ifPresent(library -> {
						autowire.accept(JavaPlugin.TEST_IMPLEMENTATION_CONFIGURATION_NAME, library);
					});
			libs.findLibrary(MOCKITO_INLINE)
					.ifPresent(library -> {
						autowire.accept(JavaPlugin.TEST_IMPLEMENTATION_CONFIGURATION_NAME, library);
					});

			libs.findLibrary(AWAITILITY)
					.ifPresent(library -> {
						autowire.accept(JavaPlugin.TEST_IMPLEMENTATION_CONFIGURATION_NAME, library);
					});
		});
	}

	public void configure(final Project project, final TaskContainer tasks) {
		final ObjectFactory objects = project.getObjects();
		final ExtensionContainer extensions = project.getExtensions();
		final JavaToolchainService javaToolchains = extensions.getByType(JavaToolchainService.class);

		if (project == project.getRootProject()) {
			final String[] taskNames = {
					LifecycleBasePlugin.ASSEMBLE_TASK_NAME,
					LifecycleBasePlugin.BUILD_TASK_NAME,
					LifecycleBasePlugin.CHECK_TASK_NAME,
					LifecycleBasePlugin.CLEAN_TASK_NAME,
			};
			for (final String taskName : taskNames) {
				tasks.named(taskName).configure(task -> {
					task.dependsOn(
							project.getGradle()
									.getIncludedBuilds()
									.stream()
									.filter(includedBuild -> !Objects.equals("build-logic", includedBuild.getName()) || Objects.equals(LifecycleBasePlugin.CHECK_TASK_NAME, taskName))
									.map(includedBuild -> includedBuild.task(org.gradle.util.Path.ROOT + taskName))
									.toArray()
					);
				});
			}
		}

		tasks.configureEach(task -> {
			if (!Objects.equals(LifecycleBasePlugin.CLEAN_TASK_NAME, task.getName())) {
				task.mustRunAfter(LifecycleBasePlugin.CLEAN_TASK_NAME);
			}
		});

		tasks.withType(JavaCompile.class).configureEach(task -> {
			final CompileOptions options = task.getOptions();
			options.getCompilerArgs().add("-parameters");
			options.setEncoding(StandardCharsets.UTF_8.displayName());
		});

		tasks.withType(Test.class).configureEach(task -> {
			task.finalizedBy("jacoco" + KoszyGradleUtil.capitalize(task.getName()) + "Report");

			task.useJUnitPlatform();

			task.jvmArgs("-ea");

			final TestLoggingContainer logging = task.getTestLogging();
			logging.setShowCauses(true);
			logging.setShowExceptions(true);
			logging.setShowStackTraces(true);
			logging.setShowStandardStreams(true);
		});

		tasks.withType(Jar.class).configureEach(task -> {
			task.dependsOn(GenerateGitPropertiesTask.TASK_NAME);
		});

		tasks.withType(Checkstyle.class).configureEach(task -> {
			task.getJavaLauncher().set(javaToolchains.launcherFor(toolchain -> {
				toolchain.getLanguageVersion().set(JavaLanguageVersion.current());
			}));
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

		if (project == project.getRootProject()) {
			tasks.register("allDependencyUpdates").configure(task -> {
				task.setDescription(
						tasks.withType(DependencyUpdatesTask.class)
								.stream()
								.findFirst()
								.map(DefaultTask::getDescription)
								.orElse(null)
				);
				task.setGroup(HelpTasksPlugin.HELP_GROUP);
				task.dependsOn(
						project.getGradle()
								.getIncludedBuilds()
								.stream()
								.map(includedBuild -> includedBuild.task(org.gradle.util.Path.ROOT + "dependencyUpdates"))
								.toArray()
				);
			});
		}
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
