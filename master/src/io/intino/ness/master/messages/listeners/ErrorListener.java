package io.intino.ness.master.messages.listeners;

import io.intino.ness.master.messages.MasterMessageException;

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
		MasterMessageException cause();

		/**The client name that sent the request.*/
		default String clientName() {
			MasterMessageException error = cause();
			return error == null ? null : error.clientName();
		}

		/**The message id that triggered the error.*/
		default String messageId() {
			MasterMessageException error = cause();
			return error == null ? null : error.originalMessageId();
		}
	}
}
