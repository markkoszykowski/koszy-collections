package com.koszy.gradle;

import org.gradle.api.Project;
import org.gradle.api.UnknownProjectException;
import org.gradle.api.artifacts.ProjectDependency;
import org.gradle.api.artifacts.VersionCatalog;
import org.gradle.api.artifacts.VersionCatalogsExtension;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.internal.artifacts.DefaultProjectDependencyFactory;
import org.gradle.api.internal.notations.ProjectDependencyFactory;
import org.gradle.api.internal.project.ProjectInternal;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.plugins.ExtensionContainer;
import org.gradle.internal.Pair;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.function.Predicate;

public final class KoszyGradleUtil {

	public static final String LIBS = "libs";

	public static boolean isBlank(final String string) {
		return string == null || string.isBlank();
	}

	/// @see org.gradle.internal.typeconversion.MapNotationConverter#get(Map, String)
	public static String get(final Map<String, ?> args, final String key) {
		final Object value = args.get(key);
		final String string = value == null ? null : value.toString();
		return string == null || string.isEmpty() ? null : string;
	}

	public static <T> Callable<T> constant(final T t) {
		return () -> t;
	}

	@FunctionalInterface
	public interface ThrowingFunction<T, R> {
		R apply(T t) throws Exception;
	}

	public static <T, R> Function<T, R> function(final ThrowingFunction<? super T, ? extends R> function) {
		return t -> {
			try {
				return function.apply(t);
			} catch (final RuntimeException exception) {
				throw exception;
			} catch (final Exception exception) {
				throw new RuntimeException(exception);
			}
		};
	}

	public static <T1, T2, R1, R2> Function<? super Pair<T1, T2>, ? extends Pair<R1, R2>> function(
			final Function<? super T1, ? extends R1> left,
			final Function<? super T2, ? extends R2> right
	) {
		return pair -> Pair.of(left.apply(pair.left), right.apply(pair.right));
	}

	public static <T1, T2, R1> Function<? super Pair<T1, T2>, ? extends Pair<R1, T2>> leftFunction(
			final Function<? super T1, ? extends R1> left
	) {
		return pair -> Pair.of(left.apply(pair.left), pair.right);
	}

	public static <T1, T2, R2> Function<? super Pair<T1, T2>, ? extends Pair<T1, R2>> rightFunction(
			final Function<? super T2, ? extends R2> right
	) {
		return pair -> Pair.of(pair.left, right.apply(pair.right));
	}

	public static <T1, T2> Predicate<? super Pair<T1, T2>> leftPredicate(
			final Predicate<? super T1> left
	) {
		return pair -> left.test(pair.left);
	}

	public static <T1, T2> Predicate<? super Pair<T1, T2>> rightPredicate(
			final Predicate<? super T2> right
	) {
		return pair -> right.test(pair.right);
	}

	public static Integer toInteger(final String string) {
		if (string == null) {
			return null;
		}
		try {
			return Integer.parseInt(string);
		} catch (final NumberFormatException exception) {
			return null;
		}
	}

	public static String capitalize(final String string) {
		if (isBlank(string)) {
			return string;
		}
		final char first = string.charAt(0);
		final char upper = Character.toUpperCase(first);
		return first == upper ? (string) : (upper + string.substring(1));
	}

	public static Optional<Path> find(final Path haystack, final String needle) {
		Path current = haystack;
		while (current != null && !Files.exists(current.resolve(needle))) {
			current = current.getParent();
		}
		return Optional.ofNullable(current).map(path -> path.resolve(needle));
	}

	public static Optional<Path> find(final File haystack, final String needle) {
		return Optional.ofNullable(haystack)
				.map(file -> {
					try {
						return file.getCanonicalFile().toPath();
					} catch (final IOException exception) {
						return file.toPath().normalize();
					}
				})
				.flatMap(path -> find(path, needle));
	}

	public static Optional<VersionCatalog> libs(final ExtensionAware extendable) {
		final ExtensionContainer extensions = extendable.getExtensions();
		return Optional.ofNullable(extensions.findByType(VersionCatalogsExtension.class))
				.flatMap(extension -> extension.find(KoszyGradleUtil.LIBS));
	}

	/// @see DependencyHandler#project(String)
	public static ProjectDependency includedBuild(final Project project, final String path) {
		return includedBuild(project, path, null);
	}

	/// @see DependencyHandler#project(Map)
	public static ProjectDependency includedBuild(final Project project, final Map<String, ?> notation) {
		return includedBuild(project, get(notation, "path"), get(notation, "configuration"));
	}

	private static ProjectDependency includedBuild(final Project project, final String path, final String configuration) {
		return Optional.ofNullable(project instanceof final ProjectInternal internal ? internal : null)
				.map(ProjectInternal::getServices)
				.map(services -> services.get(DefaultProjectDependencyFactory.class))
				.map(factory -> includedBuild(factory, path, configuration))
				.orElseThrow(() -> new UnknownProjectException(String.format("Project with path '%s' could not be found.", path)));
	}

	/// @see ProjectDependencyFactory.ProjectDependencyMapNotationConverter#parseMap(String, String)
	private static ProjectDependency includedBuild(final DefaultProjectDependencyFactory factory, final String path, final String configuration) {
		final ProjectDependency dependency = factory.create(org.gradle.util.Path.path(path));
		if (configuration != null) {
			dependency.setTargetConfiguration(configuration);
		}
		return dependency;
	}

	private KoszyGradleUtil() {
	}

}
