package io.intino.ness.core.fs;

import io.intino.alexandria.TripleStore;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class FSMetadata {

	private static Map<File, TripleStore> tripleStores = new HashMap<>();

	public static Stream<String[]> of(FSSet set) {
		return metadataTripleStoreOf(set).matches(set.name());
	}

	private static TripleStore metadataTripleStoreOf(FSSet set) {
		if (!isOpenMetadataTripleStoreOf(set)) openMetadataTripleStoreOf(set);
		return getMetadataTripleStoreOf(set);
	}

	private static TripleStore getMetadataTripleStoreOf(FSSet set) {
		return tripleStores.get(metadataFileOf(set));
	}

	private static boolean isOpenMetadataTripleStoreOf(FSSet set) {
		return tripleStores.containsKey(metadataFileOf(set));
	}

	private static void openMetadataTripleStoreOf(FSSet set) {
		TripleStore tripleStore = new TripleStore(metadataFileOf(set));
		tripleStores.put(tripleStore.file(), tripleStore);
	}

	private static File metadataFileOf(FSSet set) {
		return new File(tubFolderOf(set.file()), FSSetStore.MetadataFilename);
	}

	private static File tubFolderOf(File file) {
		return file.getParentFile();
	}


}
