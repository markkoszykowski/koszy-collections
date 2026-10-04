package com.koszy.util;

public class RequirementNotSatisfiedException extends RuntimeException {

	public RequirementNotSatisfiedException() {
		super();
	}

	public RequirementNotSatisfiedException(final String message) {
		super(message);
	}

	public RequirementNotSatisfiedException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public RequirementNotSatisfiedException(final Throwable cause) {
		super(cause);
	}

	protected RequirementNotSatisfiedException(final String message, final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
