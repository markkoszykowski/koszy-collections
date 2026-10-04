package com.koszy.collections.generator;

import com.palantir.javapoet.JavaFile;

import java.util.List;

public interface Generator {

	List<JavaFile> generateMainSources();

	List<JavaFile> generateTestSources();

}
