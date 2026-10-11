package com.koszy.gradle;

import com.github.benmanes.gradle.versions.VersionsSettingsPlugin;
import org.gradle.api.Plugin;
import org.gradle.api.initialization.Settings;
import org.gradle.api.initialization.resolve.DependencyResolutionManagement;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.plugins.PluginContainer;
import org.jspecify.annotations.NonNull;

import javax.inject.Inject;
import java.nio.file.Files;

@SuppressWarnings(value = {"unused"})
public final class KoszySettingsPlugin implements Plugin<Settings> {

	private static final Logger logger = Logging.getLogger(KoszySettingsPlugin.class);

	private final ObjectFactory objects;

	@Inject
	public KoszySettingsPlugin(final ObjectFactory objects) {
		this.objects = objects;
	}

	public void configure(final Settings settings) {
	}

	public void configure(final Settings settings, final DependencyResolutionManagement dependencyResolutionManagement) {
		KoszyGradleUtil.find(settings.getRootDir().getParentFile(), "gradle")
				.map(gradle -> gradle.resolve("libs.versions.toml"))
				.filter(Files::isRegularFile)
				.map(libs -> this.objects.fileCollection().from(libs))
				.ifPresent(libs -> {
					dependencyResolutionManagement.versionCatalogs(versionCatalogs -> {
						versionCatalogs.create(KoszyGradleUtil.LIBS, versionCatalog -> versionCatalog.from(libs));
					});
				});
	}

	public void configure(final Settings settings, final PluginContainer plugins) {
		plugins.apply(VersionsSettingsPlugin.class);
	}

	@Override
	public void apply(final @NonNull Settings target) {
		this.configure(target);
		this.configure(target, target.getDependencyResolutionManagement());
		this.configure(target, target.getPlugins());
	}

}
