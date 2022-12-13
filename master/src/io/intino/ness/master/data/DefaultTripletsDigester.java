package io.intino.ness.master.data;

import io.intino.ness.master.model.TripletRecord;
import io.intino.ness.master.serialization.MasterSerializer;

public class DefaultTripletsDigester implements MasterTripletsDigester {

	@Override
	public Result load(EntityLoader entityLoader, MasterSerializer serializer) throws Exception {
		WritableResult result = Result.create();
		entityLoader.loadTriplets(result.stats()).forEach(t -> result.records().computeIfAbsent(t.subject(), TripletRecord::new).put(t));
		return result;
	}
}
