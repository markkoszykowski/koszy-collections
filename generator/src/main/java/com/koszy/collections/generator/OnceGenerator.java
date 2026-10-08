package com.koszy.collections.generator;

import com.koszy.util.LangUtil;
import com.koszy.util.Require;
import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.FieldSpec;
import com.palantir.javapoet.JavaFile;
import com.palantir.javapoet.MethodSpec;
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
import java.util.function.BooleanSupplier;
import java.util.function.DoubleSupplier;
import java.util.function.IntSupplier;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

public final class OnceGenerator implements Generator {

	@Override
	public List<JavaFile> generateMainSources() {
		final TypeSpec.Builder once = TypeSpec.classBuilder("Once")
				.addModifiers(Modifier.PUBLIC, Modifier.FINAL)
				.addType(notThreadSafe(Type.BOOLEAN, BooleanSupplier.class)).addType(threadSafe(Type.BOOLEAN, BooleanSupplier.class))
				.addType(notThreadSafe(Type.INT, IntSupplier.class)).addType(threadSafe(Type.INT, IntSupplier.class))
				.addType(notThreadSafe(Type.LONG, LongSupplier.class)).addType(threadSafe(Type.LONG, LongSupplier.class))
				.addType(notThreadSafe(Type.DOUBLE, DoubleSupplier.class)).addType(threadSafe(Type.DOUBLE, DoubleSupplier.class))
				.addType(notThreadSafe(Type.OBJECT, Supplier.class)).addType(threadSafe(Type.OBJECT, Supplier.class))
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

	private static TypeName parameter(final Type type) {
		return type.isGeneric() ? TypeVariableName.get("T") : type.type();
	}

	private static TypeName supplier(final Type type, final Class<?> supplier, final TypeName parameter) {
		if (type.isGeneric()) {
			return ParameterizedTypeName.get(ClassName.get(supplier), WildcardTypeName.subtypeOf(parameter));
		} else {
			return ClassName.get(supplier);
		}
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

	private static TypeSpec threadSafe(final Type type, final Class<?> supplier) {
		final TypeName parameter = parameter(type);
		final TypeSpec.Builder threadSafe = TypeSpec.classBuilder("ThreadSafe" + type.prefix() + "Supplier")
				.addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL);
		if (parameter instanceof final TypeVariableName generic) {
			threadSafe.addTypeVariable(generic)
					.addSuperinterface(ParameterizedTypeName.get(ClassName.get(supplier), generic));
		} else {
			threadSafe.addSuperinterface(supplier(type, supplier, parameter));
		}

		threadSafe.addField(
						FieldSpec.builder(Object.class, "lock")
								.addModifiers(Modifier.PRIVATE, Modifier.FINAL)
								.initializer("new $T()", Object.class)
								.build()
				)
				.addField(
						FieldSpec.builder(supplier(type, supplier, parameter), "supplier")
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

		threadSafe.addMethod(
						MethodSpec.constructorBuilder()
								.addModifiers(Modifier.PRIVATE)
								.addParameter(supplier(type, supplier, parameter), "supplier", Modifier.FINAL)
								.addStatement("$N = $T.notNull($N)", GeneratorUtil.instance("supplier"), Require.class, "supplier")
								.build()
				)
				.addMethod(
						MethodSpec.methodBuilder("result")
								.addModifiers(Modifier.PRIVATE)
								.returns(parameter)
								.beginControlFlow("if ($N == $L)", GeneratorUtil.instance("throwable"), null)
								.addStatement("return $N", GeneratorUtil.instance("result"))
								.nextControlFlow("else")
								.addStatement("return $T.rethrow($N)", LangUtil.class, GeneratorUtil.instance("throwable"))
								.endControlFlow()
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
								.addStatement("return $N()", GeneratorUtil.instance("result"))
								.build()
				);

		return threadSafe.build();
	}

	private static TypeSpec notThreadSafe(final Type type, final Class<?> supplier) {
		final TypeName parameter = parameter(type);
		final TypeSpec.Builder threadSafe = TypeSpec.classBuilder("NotThreadSafe" + type.prefix() + "Supplier")
				.addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL);
		if (parameter instanceof final TypeVariableName generic) {
			threadSafe.addTypeVariable(generic)
					.addSuperinterface(ParameterizedTypeName.get(ClassName.get(supplier), generic));
		} else {
			threadSafe.addSuperinterface(supplier(type, supplier, parameter));
		}

		threadSafe.addField(
						FieldSpec.builder(supplier(type, supplier, parameter), "supplier")
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

		threadSafe.addMethod(
						MethodSpec.constructorBuilder()
								.addModifiers(Modifier.PRIVATE)
								.addParameter(supplier(type, supplier, parameter), "supplier", Modifier.FINAL)
								.addStatement("$N = $T.notNull($N)", GeneratorUtil.instance("supplier"), Require.class, "supplier")
								.build()
				)
				.addMethod(
						MethodSpec.methodBuilder("result")
								.addModifiers(Modifier.PRIVATE)
								.returns(parameter)
								.beginControlFlow("if ($N == $L)", GeneratorUtil.instance("throwable"), null)
								.addStatement("return $N", GeneratorUtil.instance("result"))
								.nextControlFlow("else")
								.addStatement("return $T.rethrow($N)", LangUtil.class, GeneratorUtil.instance("throwable"))
								.endControlFlow()
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
								.addStatement("return $N()", GeneratorUtil.instance("result"))
								.build()
				);

		return threadSafe.build();
	}

}
