package io.intino.master.serialization;

import java.util.Map;

/**
 * Converts a record (Map<String, String>) into a string representation and vice-versa.
 * Serializers should be stateless and thread safe.
 * */
public interface MasterSerializer {

	String name();

	String serialize(Map<String, String> record);

	Map<String, String> deserialize(String str);
}
