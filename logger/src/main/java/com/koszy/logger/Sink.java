package com.koszy.logger;

import java.nio.charset.Charset;

enum Sink {

	STDOUT("stdout.encoding"),
	STDERR("stderr.encoding"),
	FILE("file.encoding");

	static Charset charset(final String property) {
		try {
			final String charset = System.getProperty(property);
			if (charset != null) {
				return Charset.forName(charset);
			}
		} catch (final Exception ignored) {
		}
		return Charset.defaultCharset();
	}

	final Charset charset;

	Sink(final String property) {
		this.charset = charset(property);
	}

}
