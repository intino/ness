package io.intino.ness.master.serialization;

import io.intino.ness.master.model.TripletRecord;

import java.util.Optional;

/**
 * Converts a record (Map<String, String>) into a string representation and vice-versa.
 * Serializers should be stateless and thread safe.
 */
public interface MasterSerializer {

	String name();

	String serialize(TripletRecord record);

	TripletRecord deserialize(String str);

	default Optional<TripletRecord> tryDeserialize(String str) {
		try {
			return Optional.of(deserialize(str));
		} catch (Exception e) {
			return Optional.empty();
		}
	}
}
