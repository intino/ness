package io.intino.ness.core.sessions;

import io.intino.alexandria.TripleStore;
import io.intino.alexandria.logger.Logger;
import io.intino.alexandria.zet.ZetReader;
import io.intino.alexandria.zet.ZetStream;
import io.intino.ness.core.Blob;
import io.intino.ness.core.fs.FSSetStore;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.intino.ness.core.fs.FS.copyInto;
import static io.intino.ness.core.fs.FSSetStore.MetadataFilename;
import static io.intino.ness.core.fs.FSSetStore.SetExtension;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;

public class SetSessionManager {
	private static final String BlobExtension = ".blob";
	private final List<File> files;
	private final File storeFolder;

	private SetSessionManager(List<File> files, File storeFolder) {
		this.files = files;
		this.storeFolder = storeFolder;
	}

	public static void push(Blob blob, File stageFolder) {
		copyInto(fileFor(blob, stageFolder), blob.inputStream());
	}

	public static void seal(File stageFolder, File storeFolder) {
		new SetSessionManager(blobsOf(stageFolder), storeFolder).seal();
	}

	private static List<File> blobsOf(File stageFolder) {
		File[] files = stageFolder.listFiles(f -> f.getName().endsWith(BlobExtension));
		return files != null ? asList(files) : Collections.emptyList();
	}

	private static File fileFor(Blob blob, File parentFolder) {
		return new File(parentFolder, randomName(blob));
	}

	private static String randomName(Blob blob) {
		return randomUUID().toString() + "." + blob.type() + BlobExtension;
	}

	private static String extensionOf(Blob.Type type) {
		return "." + type.name() + BlobExtension;
	}

	private void seal() {
		sealSetSessions();
		sealSetMetadataSessions();
	}

	private void sealSetMetadataSessions() {
		Map<String, TripleStore> map = new HashMap<>();
		loadSetMetadataSessions()
				.flatMap(TripleStore::all)
				.forEach(s -> processTriple(s, map));
		map.values().forEach(TripleStore::save);
	}

	private void processTriple(String[] triple, Map<String, TripleStore> map) {
		Fingerprint fingerprint = new Fingerprint(triple[0]);
		tripleStoreFor(metadataPathOf(fingerprint), map).put(fingerprint.set(), triple[1], triple[2]);
	}

	private String metadataPathOf(Fingerprint fingerprint) {
		return fingerprint.tank() + "/" + fingerprint.timetag();
	}

	private TripleStore tripleStoreFor(String path, Map<String, TripleStore> map) {
		if (!map.containsKey(path))
			map.put(path, new TripleStore(metadataFileOf(path)));
		return map.get(path);
	}

	private File metadataFileOf(String path) {
		return new File(storeFolder, path + "/" + MetadataFilename);
	}

	private Stream<TripleStore> loadSetMetadataSessions() {
		return files.parallelStream()
				.filter(f -> f.getName().endsWith(extensionOf(Blob.Type.setMetadata)))
				.map(TripleStore::new);
	}

	private void sealSetSessions() {
		List<SetSessionFileReader> readers = loadSetSessions();
		Set<Fingerprint> distinctChunks = distinctChunks(readers);
		Logger.trace("Sets to seal " + distinctChunks.size());
		distinctChunks.parallelStream().forEach(distinctChunk -> process(distinctChunk, readers));
	}

	private void process(Fingerprint fingerprint, List<SetSessionFileReader> readers) {
		try {
			List<ZetStream> streams = chunksOf(readers, fingerprint);
			File setFile = filepath(fingerprint);
			File tempFile = File.createTempFile(fingerprint.toString(), SetExtension);
			if (setFile.exists()) streams.add(new ZetReader(setFile));
			FSSetStore.write(new ZetStream.Union(streams), tempFile);
			Files.move(tempFile.toPath(), setFile.toPath(), REPLACE_EXISTING);
		} catch (IOException e) {
			Logger.error(e);
		}
	}

	private List<SetSessionFileReader> loadSetSessions() {
		return files.parallelStream().filter(f -> f.getName().endsWith(extensionOf(Blob.Type.set))).map(f -> {
			try {
				return new SetSessionFileReader(f);
			} catch (IOException e) {
				Logger.error(e);
				return null;
			}
		}).collect(toList());
	}

	private File filepath(Fingerprint fingerprint) {
		File output = new File(storeFolder, fingerprint + SetExtension);
		output.getParentFile().mkdirs();
		return output;
	}

	private List<ZetStream> chunksOf(List<SetSessionFileReader> readers, Fingerprint fingerprint) {
		return readers.stream()
				.map(r -> r.chunks(fingerprint))
				.flatMap(Collection::stream)
				.map(SetSessionFileReader.Chunk::stream)
				.collect(toList());
	}

	private Set<Fingerprint> distinctChunks(List<SetSessionFileReader> readers) {
		return readers.stream()
				.map(SetSessionFileReader::chunks)
				.flatMap(Collection::stream)
				.map(SetSessionFileReader.Chunk::fingerprint)
				.collect(Collectors.toSet());
	}

}
