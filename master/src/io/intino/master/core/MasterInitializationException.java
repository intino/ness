package io.intino.master.core;

public class MasterInitializationException extends RuntimeException {

	public MasterInitializationException() {
	}

	public MasterInitializationException(String message) {
		super(message);
	}

	public MasterInitializationException(String message, Throwable cause) {
		super(message, cause);
	}
}
