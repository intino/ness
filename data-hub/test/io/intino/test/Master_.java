package io.intino.test;

import io.intino.alexandria.datalake.Datalake;
import io.intino.alexandria.datalake.file.FileDatalake;
import io.intino.ness.master.core.Master;
import io.intino.ness.master.data.MasterTripletsDigester;
import io.intino.ness.master.data.TripletLoader;
import io.intino.ness.master.model.Triplet;

import java.io.File;
import java.util.stream.Stream;

public class Master_ {

	public static void main(String[] args) {

		FileDatalake datalake = new FileDatalake(new File("temp/datalake"));

		Master.Config config = new Master.Config();
		config.datalakeRootPath(datalake.root());
		config.tripletsLoader(new DatahubTripletLoader(datalake.tripletsStore()));

		Master master = new Master(config);
		Runtime.getRuntime().addShutdownHook(new Thread(master::stop));

		master.start();
	}

	private static class DatahubTripletLoader implements TripletLoader {

		private final Datalake.TripletStore store;

		public DatahubTripletLoader(Datalake.TripletStore store) {
			this.store = store;
		}

		@Override
		public Stream<Triplet> loadTriplets(MasterTripletsDigester.Result.Stats stats) {
			return store.tanks()
					.peek(t -> stats.increment("Tanks read"))
					.flatMap(Datalake.TripletStore.Tank::tubs)
					.flatMap(tub -> readTripletsFrom(tub, stats));
		}

		private Stream<Triplet> readTripletsFrom(Datalake.TripletStore.Tub tub, MasterTripletsDigester.Result.Stats stats) {
			stats.increment(MasterTripletsDigester.Result.Stats.FILES_READ);
			return tub.triplets()
					.map(t -> new Triplet(t.subject(), t.verb(), t.object()))
					.peek(t -> stats.increment(MasterTripletsDigester.Result.Stats.TRIPLETS_READ));
		}
	}
}
