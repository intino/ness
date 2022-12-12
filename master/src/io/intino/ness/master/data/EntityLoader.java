package io.intino.ness.master.data;

import io.intino.ness.master.model.Triplet;

import java.util.stream.Stream;

public interface EntityLoader {

	Stream<Triplet> loadTriplets(MasterTripletsDigester.Result.Stats stats) throws Exception;
}
