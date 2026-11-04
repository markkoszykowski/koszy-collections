package com.koszy.collections.generator;

import com.koszy.util.LangUtil;
import com.koszy.util.Require;
import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.FieldSpec;
import com.palantir.javapoet.JavaFile;
import com.palantir.javapoet.MethodSpec;
import com.palantir.javapoet.ParameterSpec;
import com.palantir.javapoet.ParameterizedTypeName;
import com.palantir.javapoet.TypeName;
import com.palantir.javapoet.TypeSpec;
import com.palantir.javapoet.TypeVariableName;
import com.palantir.javapoet.WildcardTypeName;

import javax.lang.model.element.Modifier;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.BooleanSupplier;
import java.util.function.DoubleSupplier;
import java.util.function.Function;
import java.util.function.IntSupplier;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

public final class OnceGenerator implements Generator {

	@Override
	public List<JavaFile> generateMainSources() {
		final TypeSpec.Builder once = TypeSpec.classBuilder("Once")
				.addModifiers(Modifier.PUBLIC, Modifier.FINAL)
				.addAnnotation(GeneratorUtil.generated(OnceGenerator.class))

				.addMethod(notThreadSafeFactory(Type.BOOLEAN, BooleanSupplier.class)).addMethod(threadSafeFactory(Type.BOOLEAN, BooleanSupplier.class))
				.addMethod(notThreadSafeFactory(Type.INT, IntSupplier.class)).addMethod(threadSafeFactory(Type.INT, IntSupplier.class))
				.addMethod(notThreadSafeFactory(Type.LONG, LongSupplier.class)).addMethod(threadSafeFactory(Type.LONG, LongSupplier.class))
				.addMethod(notThreadSafeFactory(Type.DOUBLE, DoubleSupplier.class)).addMethod(threadSafeFactory(Type.DOUBLE, DoubleSupplier.class))
				.addMethod(notThreadSafeFactory(Type.OBJECT, Supplier.class)).addMethod(threadSafeFactory(Type.OBJECT, Supplier.class))

				.addType(notThreadSafeClass(Type.BOOLEAN, BooleanSupplier.class)).addType(threadSafeClass(Type.BOOLEAN, BooleanSupplier.class))
				.addType(notThreadSafeClass(Type.INT, IntSupplier.class)).addType(threadSafeClass(Type.INT, IntSupplier.class))
				.addType(notThreadSafeClass(Type.LONG, LongSupplier.class)).addType(threadSafeClass(Type.LONG, LongSupplier.class))
				.addType(notThreadSafeClass(Type.DOUBLE, DoubleSupplier.class)).addType(threadSafeClass(Type.DOUBLE, DoubleSupplier.class))
				.addType(notThreadSafeClass(Type.OBJECT, Supplier.class)).addType(threadSafeClass(Type.OBJECT, Supplier.class))

				.addMethod(GeneratorUtil.constructor());

		return List.of(
				JavaFile.builder("com.koszy.util", once.build())
						.indent(GeneratorUtil.indent())
						.skipJavaLangImports(true)
						.build()
		);
	}

	@Override
	public List<JavaFile> generateTestSources() {
		return Collections.emptyList();
	}

	private static ClassName notThreadSafeClassName(final Type type) {
		return ClassName.bestGuess("NotThreadSafe" + type.prefix() + "Supplier");
	}

	private static ClassName threadSafeClassName(final Type type) {
		return ClassName.bestGuess("ThreadSafe" + type.prefix() + "Supplier");
	}

	private static TypeName parameter(final Type type) {
		return type.isGeneric() ? TypeVariableName.get("T") : type.type();
	}

	private static TypeName supplier(final Type type, final Class<?> supplier, final TypeName parameter) {
		return type.isGeneric() ? ParameterizedTypeName.get(ClassName.get(supplier), parameter) : ClassName.get(supplier);
	}

	private static TypeName supplierWildcard(final Type type, final Class<?> supplier, final TypeName parameter) {
		return type.isGeneric() ? ParameterizedTypeName.get(ClassName.get(supplier), WildcardTypeName.subtypeOf(parameter)) : ClassName.get(supplier);
	}

	private static TypeName notThreadSafe(final Type type, final TypeName parameter) {
		return type.isGeneric() ? ParameterizedTypeName.get(threadSafeClassName(type), parameter) : threadSafeClassName(type);
	}

	private static TypeName threadSafe(final Type type, final TypeName parameter) {
		return type.isGeneric() ? ParameterizedTypeName.get(threadSafeClassName(type), parameter) : threadSafeClassName(type);
	}

	private static TypeName notThreadSafeWildcard(final Type type) {
		return type.isGeneric() ? ParameterizedTypeName.get(notThreadSafeClassName(type), WildcardTypeName.subtypeOf(Object.class)) : notThreadSafeClassName(type);
	}

	private static TypeName threadSafeWildcard(final Type type) {
		return type.isGeneric() ? ParameterizedTypeName.get(threadSafeClassName(type), WildcardTypeName.subtypeOf(Object.class)) : threadSafeClassName(type);
	}

	private static String method(final Class<?> supplier) {
		try {
			return Arrays.stream(supplier.getDeclaredMethods())
					.map(Method::getName)
					.findFirst()
					.orElseThrow(NoSuchMethodException::new);
		} catch (final NoSuchMethodException ex) {
			return LangUtil.rethrow(ex);
		}
	}

	private static MethodSpec factory(final Type type, final Class<?> supplier, final String method, final Function<? super Type, ? extends TypeName> wildcard, final BiFunction<? super Type, ? super TypeName, ? extends TypeName> typed) {
		final TypeName parameter = parameter(type);
		final MethodSpec.Builder builder = MethodSpec.methodBuilder(method)
				.addModifiers(Modifier.PUBLIC, Modifier.STATIC);
		if (parameter instanceof final TypeVariableName generic) {
			builder.addAnnotation(GeneratorUtil.suppress("unchecked", "checkstyle:AnnotationUseStyle"))
					.addTypeVariable(generic);
		}
		builder.returns(supplier(type, supplier, parameter))
				.addParameter(
						ParameterSpec.builder(supplierWildcard(type, supplier, parameter), "supplier")
								.addModifiers(Modifier.FINAL)
								.build()
				);
		if (type.isGeneric()) {
			builder.addStatement(
					"return $N instanceof $T ? ($T) $N : new $T($N)",
					"supplier", wildcard.apply(type),
					supplier(type, supplier, parameter), "supplier",
					typed.apply(type, parameter), "supplier"
			);
		} else {
			builder.addStatement(
					"return $N instanceof $T ? $N : new $T($N)",
					"supplier", wildcard.apply(type),
					"supplier",
					typed.apply(type, parameter), "supplier"
			);
		}
		return builder.build();
	}

	private static MethodSpec notThreadSafeFactory(final Type type, final Class<?> supplier) {
		return factory(type, supplier, "notThreadSafe", OnceGenerator::notThreadSafeWildcard, OnceGenerator::notThreadSafe);
	}

	private static MethodSpec threadSafeFactory(final Type type, final Class<?> supplier) {
		return factory(type, supplier, "threadSafe", OnceGenerator::threadSafeWildcard, OnceGenerator::threadSafe);
	}

	private static TypeSpec notThreadSafeClass(final Type type, final Class<?> supplier) {
		final TypeName parameter = parameter(type);
		final TypeSpec.Builder builder = TypeSpec.classBuilder(notThreadSafeClassName(type))
				.addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL);
		if (parameter instanceof final TypeVariableName generic) {
			builder.addTypeVariable(generic);
		}

		builder.addSuperinterface(supplier(type, supplier, parameter));

		builder.addField(
						FieldSpec.builder(supplierWildcard(type, supplier, parameter), "supplier")
								.addModifiers(Modifier.PRIVATE)
								.build()
				)
				.addField(
						FieldSpec.builder(boolean.class, "invoked")
								.addModifiers(Modifier.PRIVATE)
								.initializer("$L", false)
								.build()
				)
				.addField(
						FieldSpec.builder(parameter, "result")
								.addModifiers(Modifier.PRIVATE)
								.build()
				)
				.addField(
						FieldSpec.builder(Throwable.class, "throwable")
								.addModifiers(Modifier.PRIVATE)
								.build()
				);

		builder.addMethod(
						MethodSpec.constructorBuilder()
								.addModifiers(Modifier.PRIVATE)
								.addParameter(supplierWildcard(type, supplier, parameter), "supplier", Modifier.FINAL)
								.addStatement("$N = $T.nonNull($N)", GeneratorUtil.instance("supplier"), Require.class, "supplier")
								.build()
				)
				.addMethod(
						MethodSpec.methodBuilder(method(supplier))
								.addModifiers(Modifier.PUBLIC)
								.returns(parameter)
								.addAnnotation(Override.class)
								.beginControlFlow("if (!$N)", GeneratorUtil.instance("invoked"))
								.beginControlFlow("try")
								.addStatement("$N = $N.$N()", GeneratorUtil.instance("result"), GeneratorUtil.instance("supplier"), method(supplier))
								.nextControlFlow("catch (final $T $N)", Throwable.class, "throwable")
								.addStatement("$N = $N", GeneratorUtil.instance("throwable"), "throwable")
								.nextControlFlow("finally")
								.addStatement("$N = $L", GeneratorUtil.instance("supplier"), null)
								.addStatement("$N = $L", GeneratorUtil.instance("invoked"), true)
								.endControlFlow()
								.endControlFlow()
								.addStatement(
										"return $N == $L ? $N : $T.rethrow($N)",
										GeneratorUtil.instance("throwable"), null,
										GeneratorUtil.instance("result"),
										LangUtil.class, GeneratorUtil.instance("throwable")
								)
								.build()
				);

		return builder.build();
	}

	private static TypeSpec threadSafeClass(final Type type, final Class<?> supplier) {
		final TypeName parameter = parameter(type);
		final TypeSpec.Builder builder = TypeSpec.classBuilder(threadSafeClassName(type))
				.addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL);
		if (parameter instanceof final TypeVariableName generic) {
			builder.addTypeVariable(generic)
					.addSuperinterface(ParameterizedTypeName.get(ClassName.get(supplier), generic));
		} else {
			builder.addSuperinterface(supplierWildcard(type, supplier, parameter));
		}

		builder.addField(
						FieldSpec.builder(Object.class, "lock")
								.addModifiers(Modifier.PRIVATE, Modifier.FINAL)
								.initializer("new $T()", Object.class)
								.build()
				)
				.addField(
						FieldSpec.builder(supplierWildcard(type, supplier, parameter), "supplier")
								.addModifiers(Modifier.PRIVATE)
								.build()
				)
				.addField(
						FieldSpec.builder(boolean.class, "invoked")
								.addModifiers(Modifier.PRIVATE, Modifier.VOLATILE)
								.initializer("$L", false)
								.build()
				)
				.addField(
						FieldSpec.builder(parameter, "result")
								.addModifiers(Modifier.PRIVATE)
								.build()
				)
				.addField(
						FieldSpec.builder(Throwable.class, "throwable")
								.addModifiers(Modifier.PRIVATE)
								.build()
				);

		builder.addMethod(
						MethodSpec.constructorBuilder()
								.addModifiers(Modifier.PRIVATE)
								.addParameter(supplierWildcard(type, supplier, parameter), "supplier", Modifier.FINAL)
								.addStatement("$N = $T.nonNull($N)", GeneratorUtil.instance("supplier"), Require.class, "supplier")
								.build()
				)
				.addMethod(
						MethodSpec.methodBuilder(method(supplier))
								.addModifiers(Modifier.PUBLIC)
								.returns(parameter)
								.addAnnotation(Override.class)
								.beginControlFlow("if (!$N)", GeneratorUtil.instance("invoked"))
								.beginControlFlow("synchronized ($N)", GeneratorUtil.instance("lock"))
								.beginControlFlow("if (!$N)", GeneratorUtil.instance("invoked"))
								.beginControlFlow("try")
								.addStatement("$N = $N.$N()", GeneratorUtil.instance("result"), GeneratorUtil.instance("supplier"), method(supplier))
								.nextControlFlow("catch (final $T $N)", Throwable.class, "throwable")
								.addStatement("$N = $N", GeneratorUtil.instance("throwable"), "throwable")
								.nextControlFlow("finally")
								.addStatement("$N = $L", GeneratorUtil.instance("supplier"), null)
								.addStatement("$N = $L", GeneratorUtil.instance("invoked"), true)
								.endControlFlow()
								.endControlFlow()
								.endControlFlow()
								.endControlFlow()
								.addStatement(
										"return $N == $L ? $N : $T.rethrow($N)",
										GeneratorUtil.instance("throwable"), null,
										GeneratorUtil.instance("result"),
										LangUtil.class, GeneratorUtil.instance("throwable")
								)
								.build()
				);

		return builder.build();
	}

}
