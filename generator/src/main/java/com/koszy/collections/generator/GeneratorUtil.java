package com.koszy.collections.generator;

import com.koszy.util.LangUtil;
import com.koszy.util.Require;
import com.palantir.javapoet.AnnotationSpec;
import com.palantir.javapoet.MethodSpec;

import javax.annotation.processing.Generated;
import javax.lang.model.element.Modifier;
import java.time.Instant;

public final class GeneratorUtil {

	private static final String INDENT = "\t";

	public static String indent() {
		return INDENT;
	}

	public static String instance(final String name) {
		return String.format("%s.%s", "this", Require.nonNull(name));
	}

	public static AnnotationSpec generated(final Class<?> clazz) {
		return AnnotationSpec.builder(Generated.class)
				.addMember("value", "{$S}", clazz.getName())
				.addMember("date", "$S", Instant.now())
				.build();
	}

	public static AnnotationSpec suppress(final String... warnings) {
		final StringBuilder builder = new StringBuilder();
		String prefix = "";
		builder.append("{");
		for (int i = 0; i != warnings.length; ++i) {
			builder.append(prefix).append("$S");
			prefix = ", ";
		}
		builder.append("}");
		return AnnotationSpec.builder(SuppressWarnings.class)
				.addMember("value", builder.toString(), (Object[]) warnings)
				.build();
	}

	public static MethodSpec constructor() {
		return MethodSpec.constructorBuilder()
				.addModifiers(Modifier.PRIVATE)
				.addStatement("$T.unreachable()", LangUtil.class)
				.build();
	}

	private GeneratorUtil() {
		LangUtil.unreachable();
	}

}
