package io.intino.ness.core.sessions;

import io.intino.alexandria.logger.Logger;
import io.intino.alexandria.triplestore.FileTripleStore;
import io.intino.alexandria.triplestore.MemoryTripleStore;
import io.intino.alexandria.triplestore.TripleStore;
import io.intino.alexandria.zet.ZetReader;
import io.intino.alexandria.zet.ZetStream;
import io.intino.ness.core.Blob;
import io.intino.ness.core.fs.FSDatalake;

import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import static io.intino.ness.core.fs.FS.copyInto;
import static io.intino.ness.core.fs.FSSetStore.MetadataFilename;
import static io.intino.ness.core.fs.FSSetStore.SetExtension;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

public class SetSessionManager {
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
		File[] files = stageFolder.listFiles(f -> f.getName().endsWith(FSDatalake.BlobExtension));
		return files != null ? asList(files) : Collections.emptyList();
	}

	private static File fileFor(Blob blob, File parentFolder) {
		return new File(parentFolder, filename(blob));
	}

	private static String filename(Blob blob) {
		return blob.name() + FSDatalake.BlobExtension;
	}

	private static String extensionOf(Blob.Type type) {
		return "." + type.name() + FSDatalake.BlobExtension;
	}

	private void seal() {
		sealSetSessions();
		sealSetMetadataSessions();
	}

	private void sealSetMetadataSessions() {
		Map<String, FileTripleStore> map = new HashMap<>();
		loadSetMetadataSessions()
				.flatMap(TripleStore::all)
				.forEach(s -> processTriple(s, map));
		map.values().forEach(FileTripleStore::save);
	}

	private void processTriple(String[] triple, Map<String, FileTripleStore> map) {
		Fingerprint fingerprint = new Fingerprint(triple[0]);
		tripleStoreFor(metadataPathOf(fingerprint), map).put(fingerprint.set(), triple[1], triple[2]);
	}

	private String metadataPathOf(Fingerprint fingerprint) {
		return fingerprint.tank() + "/" + fingerprint.timetag();
	}

	private TripleStore tripleStoreFor(String path, Map<String, FileTripleStore> map) {
		if (!map.containsKey(path))
			map.put(path, new FileTripleStore(metadataFileOf(path)));
		return map.get(path);
	}

	private File metadataFileOf(String path) {
		return new File(storeFolder, path + "/" + MetadataFilename);
	}

	private Stream<MemoryTripleStore> loadSetMetadataSessions() {
		return files.parallelStream()
				.filter(f -> f.getName().endsWith(extensionOf(Blob.Type.setMetadata)))
				.map(f -> new MemoryTripleStore(zipStreamOf(f)));
	}

	private InputStream zipStreamOf(File file) {
		try {
			return new GZIPInputStream(new BufferedInputStream(new FileInputStream(file)));
		} catch (IOException e) {
			Logger.error(e);
			return null;
		}
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
			int count = write(new ZetStream.Union(streams), tempFile);
			writeSizeInMetadata(fingerprint, count);
			Files.move(tempFile.toPath(), setFile.toPath(), REPLACE_EXISTING);
		} catch (IOException e) {
			Logger.error(e);
		}
	}

	private void writeSizeInMetadata(Fingerprint fingerprint, int count) throws FileNotFoundException {
		TripleStore.Builder builder = new TripleStore.Builder(new FileOutputStream(metadataFileOf(metadataPathOf(fingerprint)), true));
		builder.put(fingerprint.set(), "_size_", count);
		builder.close();
	}

	private int write(ZetStream.Union stream, File file) throws IOException {
		file.getParentFile().mkdirs();
		DataOutputStream dataStream = new DataOutputStream(new BufferedOutputStream(new GZIPOutputStream(new FileOutputStream(file))));
		int count = 0;
		while (stream.hasNext()) {
			dataStream.writeLong(stream.next());
			count++;
		}
		dataStream.close();
		return count;
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
				.flatMap(r -> r)
				.map(SetSessionFileReader.Chunk::stream)
				.collect(toList());
	}

	private Set<Fingerprint> distinctChunks(List<SetSessionFileReader> readers) {
		return readers.stream()
				.map(SetSessionFileReader::chunks)
				.flatMap(r -> r)
				.map(SetSessionFileReader.Chunk::fingerprint)
				.collect(Collectors.toSet());
	}

}