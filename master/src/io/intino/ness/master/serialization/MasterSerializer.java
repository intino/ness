package io.intino.ness.master.serialization;

import io.intino.ness.master.model.TripletRecord;

import java.util.Map;

/**
 * Converts a record (Map<String, String>) into a string representation and vice-versa.
 * Serializers should be stateless and thread safe.
 * */
public interface MasterSerializer {

	String name();

	String serialize(TripletRecord record);

	TripletRecord deserialize(String str);
}
