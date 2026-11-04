package com.koszy.util.idle;

public interface IdleStrategy {

	void idle();

	void idle(int work);

	void reset();

}
