package io.intino.ness.master.messages.listeners;

import java.time.Instant;

@FunctionalInterface
public interface ErrorListener {

	void notify(Error error);

	/**
	 * Represents an error occurred on server side while processing an update request.
	 * */
	interface Error {

		/**The instant at which the error occurred.**/
		Instant ts();

		/**The exception thrown. **/
		Throwable cause();

		/**The client name that sent the request.*/
		String clientName();

		/**The message id that triggered the error.*/
		String messageId();
	}
}
