package com.koszy.util.buffer;

public interface RingBuffer {

	int PADDING_TYPE = -1;

	int INSUFFICIENT_CAPACITY = -1;

	int maxMessageLength();

	AtomicBuffer buffer();

	int capacity();

	int size();

	long producerPosition();

	long consumerPosition();

	boolean write(int type, DirectBuffer buffer, int offset, int length);

	int claim(int type, int length);

	void commit(int index);

	void abort(int index);

	int read(MessageHandler handler);

	int read(MessageHandler handler, int limit);

	int read(ControlledMessageHandler handler);

	int read(ControlledMessageHandler handler, int limit);

}
