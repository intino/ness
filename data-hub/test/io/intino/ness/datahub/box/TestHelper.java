package io.intino.ness.datahub.box;

import io.intino.tara.io.Stash;
import io.intino.tara.magritte.stores.InMemoryFileStore;

import java.io.File;

class TestHelper {

	static io.intino.tara.magritte.Store store(String directory) {
		return new InMemoryFileStore(new File(directory)) {
			public void writeStash(Stash stash, String path) {
				stash.language = stash.language == null || stash.language.isEmpty() ? "Ness" : stash.language;
				super.writeStash(stash, path);
			}
		};
	}
}
