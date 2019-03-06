package io.intino.ness.core.local;

import io.intino.alexandria.triplestore.FileTripleStore;
import io.intino.alexandria.triplestore.TripleStore;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class LocalMetadata {

	private static Map<File, TripleStore> tripleStores = new HashMap<>();

	public static Stream<String[]> of(LocalSet set) {
		return metadataTripleStoreOf(set).matches(set.name());
	}

	private static TripleStore metadataTripleStoreOf(LocalSet set) {
		if (!isOpenMetadataTripleStoreOf(set)) openMetadataTripleStoreOf(set);
		return getMetadataTripleStoreOf(set);
	}

	private static TripleStore getMetadataTripleStoreOf(LocalSet set) {
		return tripleStores.get(metadataFileOf(set));
	}

	private static boolean isOpenMetadataTripleStoreOf(LocalSet set) {
		return tripleStores.containsKey(metadataFileOf(set));
	}

	private static void openMetadataTripleStoreOf(LocalSet set) {
		FileTripleStore tripleStore = new FileTripleStore(metadataFileOf(set));
		tripleStores.put(tripleStore.file(), tripleStore);
	}

	private static File metadataFileOf(LocalSet set) {
		return new File(tubFolderOf(set.file()), LocalSetStore.MetadataFilename);
	}

	private static File tubFolderOf(File file) {
		return file.getParentFile();
	}


}
