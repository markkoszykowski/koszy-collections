package com.koszy.collections.generator;

import com.koszy.util.Require;
import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.TypeName;

public enum Type {

	BOOLEAN("Boolean", boolean.class, TypeName.BOOLEAN, false),
	BYTE("Byte", byte.class, TypeName.BYTE, false),
	SHORT("Short", short.class, TypeName.SHORT, false),
	INT("Int", int.class, TypeName.INT, false),
	LONG("Long", long.class, TypeName.LONG, false),
	CHAR("Char", char.class, TypeName.CHAR, false),
	FLOAT("Float", float.class, TypeName.FLOAT, false),
	DOUBLE("Double", double.class, TypeName.DOUBLE, false),
	OBJECT("Object", Object.class, ClassName.get(Object.class), true),
	REFERENCE("Reference", Object.class, ClassName.get(Object.class), true);

	private final String prefix;
	private final Class<?> clazz;
	private final TypeName type;
	private final boolean generic;

	Type(
			final String prefix,
			final Class<?> clazz,
			final TypeName type,
			final boolean generic
	) {
		this.prefix = Require.nonNull(prefix);
		this.clazz = Require.nonNull(clazz);
		this.type = Require.nonNull(type);
		this.generic = generic;
	}

	public String prefix() {
		return this.prefix;
	}

	public Class<?> clazz() {
		return this.clazz;
	}

	public TypeName type() {
		return this.type;
	}

	public boolean isGeneric() {
		return this.generic;
	}

}
