package io.intino.ness.master.messages;

import io.intino.ness.master.messages.listeners.EntityListener;
import io.intino.ness.master.messages.listeners.ErrorListener;
import io.intino.ness.master.Entity;

/**
 * Represents the result of an update request to master.
 */
public interface Response<T extends Entity> {

	/**
	 * Indicates if no errors occurred while processing the request.
	 **/
	boolean success();

	/**
	 * Indicates whether the request generated any errors or not.
	 */
	default boolean failed() {
		return !success();
	}

	/**
	 * Returns the event as the result of the processed request. Only non-null if the success() is true.
	 */
	EntityListener.Event<T> event();

	/**
	 * Returns the error occurred when processing the request. Only non-null if the success() is false.
	 */
	ErrorListener.Error error();

	static <E extends Entity> Response<E> ofSuccessful(EntityListener.Event<E> event) {
		return new Response<>() {
			@Override
			public boolean success() {
				return true;
			}

			@Override
			public EntityListener.Event<E> event() {
				return event;
			}

			@Override
			public ErrorListener.Error error() {
				return null;
			}
		};
	}

	static <E extends Entity> Response<E> ofFailure(ErrorListener.Error error) {
		return new Response<>() {
			@Override
			public boolean success() {
				return false;
			}

			@Override
			public EntityListener.Event<E> event() {
				return null;
			}

			@Override
			public ErrorListener.Error error() {
				return error;
			}
		};
	}
}
