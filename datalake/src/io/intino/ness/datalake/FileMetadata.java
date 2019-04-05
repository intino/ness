package io.intino.ness.datalake;

import io.intino.alexandria.triplestore.FileTripleStore;
import io.intino.alexandria.triplestore.TripleStore;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class FileMetadata {

	private static Map<File, TripleStore> tripleStores = new HashMap<>();

	public static Stream<String[]> of(FileSet set) {
		return metadataTripleStoreOf(set).matches(set.name());
	}

	private static TripleStore metadataTripleStoreOf(FileSet set) {
		if (!isOpenMetadataTripleStoreOf(set)) openMetadataTripleStoreOf(set);
		return getMetadataTripleStoreOf(set);
	}

	private static TripleStore getMetadataTripleStoreOf(FileSet set) {
		return tripleStores.get(metadataFileOf(set));
	}

	private static boolean isOpenMetadataTripleStoreOf(FileSet set) {
		return tripleStores.containsKey(metadataFileOf(set));
	}

	private static void openMetadataTripleStoreOf(FileSet set) {
		FileTripleStore tripleStore = new FileTripleStore(metadataFileOf(set));
		tripleStores.put(tripleStore.file(), tripleStore);
	}

	private static File metadataFileOf(FileSet set) {
		return new File(tubFolderOf(set.file()), FileSetStore.MetadataFilename);
	}

	private static File tubFolderOf(File file) {
		return file.getParentFile();
	}


}
