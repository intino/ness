package io.intino.ness.box;

import io.intino.konos.Box;
import io.intino.tara.io.Stash;
import io.intino.tara.magritte.Graph;
import io.intino.tara.magritte.stores.InMemoryFileStore;

import java.io.File;
import java.util.Arrays;

public class Main {
	public static void main(String[] args) {
		new Graph(store(param(args, "ness.store"))).loadStashes("Ness");
		Box box = new NessBox(args).open();
		Runtime.getRuntime().addShutdownHook(new Thread(box::close));
	}

	private static io.intino.tara.magritte.Store store(String directory) {
		return new InMemoryFileStore(new File(directory)) {
			public void writeStash(Stash stash, String path) {
				stash.language = stash.language == null || stash.language.isEmpty() ? "Ness" : stash.language;
				super.writeStash(stash, path);
			}
		};
	}

	private static String param(String[] args, String name) {
		return Arrays.stream(args).filter(a -> a.split("=")[0].equalsIgnoreCase(name)).findFirst().orElse(null);
	}
}