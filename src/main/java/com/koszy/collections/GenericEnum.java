package com.koszy.collections;

import com.koszy.util.ArrayUtil;
import com.koszy.util.Once;
import com.koszy.util.ReflectionUtil;

import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamException;
import java.io.Serial;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Objects;
import java.util.function.Supplier;

public abstract class GenericEnum<E extends GenericEnum<E>> implements Comparable<E>, Serializable {

	private static <T extends GenericEnum<T>> boolean isEnumConstant(final Class<T> clazz, final Field field) {
		return Modifier.isPublic(field.getModifiers()) && Modifier.isStatic(field.getModifiers()) && Modifier.isFinal(field.getModifiers()) && clazz.isAssignableFrom(field.getType());
	}

	protected static <T extends GenericEnum<T>> Field[] fields(final Class<T> clazz) {
		return ArrayUtil.filter(Field[]::new, field -> isEnumConstant(clazz, field), clazz.getFields());
	}

	@SuppressWarnings(value = {"unchecked"})
	protected static <T extends GenericEnum<T>> T[] values(final Class<T> clazz) {
		final Field[] fields = fields(clazz);
		final T[] values = (T[]) Array.newInstance(clazz, fields.length);
		for (int i = 0; i != values.length; ++i) {
			values[i] = ReflectionUtil.get(fields[i]);
		}
		return values;
	}

	private static <T extends GenericEnum<T>> String name(final Field[] constants, final GenericEnum<T> value) {
		for (final Field constant : constants) {
			if (ReflectionUtil.get(constant) == value) {
				return constant.getName();
			}
		}
		throw new IllegalCallerException();
	}

	private static <T extends GenericEnum<T>> int ordinal(final T[] values, final GenericEnum<T> value) {
		for (int i = 0; i != values.length; ++i) {
			if (values[i] == value) {
				return i;
			}
		}
		throw new IllegalCallerException();
	}

	private final Supplier<? extends GenericEnum<E>> self;

	private String name;
	private int ordinal;

	private int hash = 0;

	protected GenericEnum(final E[] values) {
		this.self = Once.threadSafe(() -> {
			this.name = name(fields(this.getDeclaringClass()), this);
			this.ordinal = ordinal(values, this);
			return this;
		});
	}

	private GenericEnum<E> self() {
		return this.self.get();
	}

	public final String name() {
		return this.self().name;
	}

	public final int ordinal() {
		return this.self().ordinal;
	}

	protected final Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}

	@SuppressWarnings(value = {"unchecked"})
	public final Class<E> getDeclaringClass() {
		final Class<?> clazz = this.getClass();
		final Class<?> zuper = clazz.getSuperclass();
		return Objects.equals(GenericEnum.class, zuper) ? (Class<E>) clazz : (Class<E>) zuper;
	}

	@Override
	public final int hashCode() {
		int hash = this.hash;
		if (hash == 0) {
			hash = this.hash = System.identityHashCode(this);
		}
		return hash;
	}

	@Override
	public final int compareTo(final E e) {
		final GenericEnum<E> that = e;
		if (!Objects.equals(this.getClass(), that.getClass()) && !Objects.equals(this.getDeclaringClass(), that.getDeclaringClass())) {
			throw new ClassCastException();
		}
		return this.ordinal() - that.ordinal();
	}

	@Override
	public String toString() {
		return this.name();
	}

	@Deprecated(since = "18", forRemoval = true)
	@SuppressWarnings(value = {"removal"})
	protected final void finalize() {
	}

	@Serial
	private void readObject(final ObjectInputStream in) throws ObjectStreamException {
		throw new InvalidObjectException("can't deserialize enum");
	}

	@Serial
	private void readObjectNoData() throws ObjectStreamException {
		throw new InvalidObjectException("can't deserialize enum");
	}

}
