package com.koszy.util.string;

public interface MutableCharSequence extends CharSequence, Appendable {

	MutableCharSequence append(boolean b);

	@Override
	MutableCharSequence append(char c);

	MutableCharSequence appendCodePoint(int c);

	MutableCharSequence append(int i);

	MutableCharSequence append(long l);

	MutableCharSequence append(float f);

	MutableCharSequence append(double d);

	@Override
	MutableCharSequence append(CharSequence string);

	@Override
	MutableCharSequence append(CharSequence string, int begin, int end);

	MutableCharSequence append(char[] string);

	MutableCharSequence append(char[] string, int offset, int length);

	MutableCharSequence append(Object object);

	MutableCharSequence length(int length);

	MutableCharSequence trim();

	MutableCharSequence clear();

}
