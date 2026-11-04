package com.koszy.util.buffer;

@FunctionalInterface
public interface MessageHandler {

	void handle(int type, DirectBuffer buffer, int offset, int length);

}
