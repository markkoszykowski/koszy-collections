package com.koszy.util.buffer;

@FunctionalInterface
public interface ControlledMessageHandler {

	enum Action {
		ABORT,
		BREAK,
		COMMIT,
		CONTINUE,
	}

	Action handle(int type, DirectBuffer buffer, int offset, int length);

}
