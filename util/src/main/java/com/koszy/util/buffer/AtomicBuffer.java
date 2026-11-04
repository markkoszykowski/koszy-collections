package com.koszy.util.buffer;

import com.koszy.util.NumberUtil;

public interface AtomicBuffer extends MutableDirectBuffer {

	int ALIGNMENT = NumberUtil.LONG_BYTES;

	String STRICT_ALIGNMENT_CHECKS_PROP_NAME = "agrona.strict.alignment.checks";


	void verifyAlignment();


	byte getByteSeqCst(int index);

	void putByteSeqCst(int index, byte value);


	char getCharSeqCst(int index);

	void putCharSeqCst(int index, char value);


	short getShortSeqCst(int index);

	void putShortSeqCst(int index, short value);


	int getIntSeqCst(int index);

	void putIntSeqCst(int index, int value);


	int getIntAcquire(int index);

	void putIntRelease(int index, int value);


	int getIntRelaxed(int index);

	void putIntRelaxed(int index, int value);


	boolean compareAndSetInt(int index, int expected, int value);

	int getAndSetInt(int index, int value);

	int getAndAddInt(int index, int delta);


	long getLongSeqCst(int index);

	void putLongSeqCst(int index, long value);


	long getLongAcquire(int index);

	void putLongRelease(int index, long value);


	long getLongRelaxed(int index);

	void putLongRelaxed(int index, long value);


	boolean compareAndSetLong(int index, long expected, long value);

	long getAndSetLong(int index, long value);

	long getAndAddLong(int index, long delta);

}
