package com.koszy.collections.generator;

import com.koszy.util.LangUtil;
import com.koszy.util.Require;
import com.palantir.javapoet.MethodSpec;

import javax.lang.model.element.Modifier;

public final class GeneratorUtil {

	private static final String INDENT = "\t";

	public static String indent() {
		return INDENT;
	}

	public static String instance(final String name) {
		return String.format("%s.%s", "this", Require.notNull(name));
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
