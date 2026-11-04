package com.koszy.util.buffer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public interface DirectBuffer extends Comparable<DirectBuffer> {


	String DISABLE_BOUNDS_CHECKS_PROP_NAME = "koszy.disable.bounds.checks";

	boolean SHOULD_BOUNDS_CHECK = !"true".equals(System.getProperty(DISABLE_BOUNDS_CHECKS_PROP_NAME));


	void wrap(byte[] array);

	void wrap(byte[] array, int offset, int length);

	void wrap(ByteBuffer buffer);

	void wrap(ByteBuffer buffer, int offset, int length);

	void wrap(DirectBuffer buffer);

	void wrap(DirectBuffer buffer, int offset, int length);

	void wrap(long address, int length);


	ByteBuffer buffer();

	long address();

	byte[] array();

	int capacity();

	int adjustment();


	void check(int index);

	void check(int offset, int length);


	byte getByte(int index);

	char getChar(int index);

	char getChar(int index, ByteOrder byteOrder);

	char getCharUnaligned(int index);

	char getCharUnaligned(int index, ByteOrder byteOrder);

	short getShort(int index);

	short getShort(int index, ByteOrder byteOrder);

	short getShortUnaligned(int index);

	short getShortUnaligned(int index, ByteOrder byteOrder);

	int getInt(int index);

	int getInt(int index, ByteOrder byteOrder);

	int getIntUnaligned(int index);

	int getIntUnaligned(int index, ByteOrder byteOrder);

	long getLong(int index);

	long getLong(int index, ByteOrder byteOrder);

	long getLongUnaligned(int index);

	long getLongUnaligned(int index, ByteOrder byteOrder);

	float getFloat(int index);

	float getFloat(int index, ByteOrder byteOrder);

	double getDouble(int index);

	double getDouble(int index, ByteOrder byteOrder);


	void getBytes(int index, byte[] array);

	void getBytes(int index, byte[] array, int offset, int length);

	void getBytes(int index, ByteBuffer buffer, int length);

	void getBytes(int index, ByteBuffer buffer, int offset, int length);

	void getBytes(int index, MutableDirectBuffer buffer, int offset, int length);

}
