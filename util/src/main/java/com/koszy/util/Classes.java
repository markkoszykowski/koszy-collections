package com.koszy.util;

public final class Classes {

	public static String name(final Class<?> clazz) {
		String name;
		if (clazz == null) {
			return null;
		} else if (!(name = clazz.getSimpleName()).isEmpty()) {
			return name;
		} else if ((name = clazz.getCanonicalName()) != null) {
			return name;
		} else {
			return clazz.getTypeName();
		}
	}

	private Classes() {
		LangUtil.unreachable();
	}

}
