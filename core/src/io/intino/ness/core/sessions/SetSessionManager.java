package io.intino.ness.core.sessions;

import io.intino.alexandria.logger.Logger;
import io.intino.alexandria.triplestore.FileTripleStore;
import io.intino.alexandria.triplestore.MemoryTripleStore;
import io.intino.alexandria.triplestore.TripleStore;
import io.intino.alexandria.zet.ZetReader;
import io.intino.alexandria.zet.ZetStream;
import io.intino.alexandria.zet.ZetWriter;
import io.intino.ness.core.Blob;
import io.intino.ness.core.fs.FS;
import io.intino.ness.core.fs.FSDatalake;

import java.io.*;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;

import static io.intino.ness.core.fs.FS.copyInto;
import static io.intino.ness.core.fs.FSSetStore.MetadataFilename;
import static io.intino.ness.core.fs.FSSetStore.SetExtension;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.util.stream.Collectors.toList;

public class SetSessionManager {
	private final List<File> files;
	private final File setStoreFolder;
	private final File tempFolder;

	private SetSessionManager(List<File> files, File setStoreFolder, File tempFolder) {
		this.files = files;
		this.setStoreFolder = setStoreFolder;
		this.tempFolder = tempFolder;
	}

	public static void push(Blob blob, File stageFolder) {
		copyInto(fileFor(blob, stageFolder), blob.inputStream());
	}

	public static void seal(File stageFolder, File setStoreFolder, File tempFolder) {
		new SetSessionManager(blobsOf(stageFolder), setStoreFolder, tempFolder).seal();
	}

	private static List<File> blobsOf(File stageFolder) {
		return FS.allFilesIn(stageFolder, f -> f.getName().endsWith(FSDatalake.BlobExtension)).collect(Collectors.toList());
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
		Map<File, FileTripleStore> map = new HashMap<>();
		loadSetMetadataSessions()
				.flatMap(TripleStore::all)
				.forEach(s -> processTriple(s, map));
		map.values().forEach(FileTripleStore::save);
	}

	private void processTriple(String[] triple, Map<File, FileTripleStore> map) {
		Fingerprint fingerprint = new Fingerprint(triple[0]);
		tripleStoreFor(metadataFileOf(fingerprint), map).put(fingerprint.set(), triple[1], triple[2]);
	}

	private File metadataFileOf(Fingerprint fingerprint) {
		File file = new File(setStoreFolder, fingerprint.tank() + "/" + fingerprint.timetag() + "/" + MetadataFilename);
		file.getParentFile().mkdirs();
		return file;
	}

	private TripleStore tripleStoreFor(File file, Map<File, FileTripleStore> map) {
		if (!map.containsKey(file)) map.put(file, new FileTripleStore(file));
		return map.get(file);
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
		List<SetSessionFileReader> readers = setSessionReaders();
		fingerPrintsIn(readers).parallelStream().forEach(fp -> seal(fp, readers));
		close(readers);
	}

	private void close(List<SetSessionFileReader> readers) {
		readers.forEach(SetSessionFileReader::close);
	}

	private List<SetSessionFileReader> setSessionReaders() {
		return setBlobs().map(this::setSessionReader).collect(toList());
	}

	private SetSessionFileReader setSessionReader(File file) {
		try {
			return new SetSessionFileReader(file, this.tempFolder);
		} catch (IOException e) {
			Logger.error(e);
			return null;
		}
	}

	private void seal(Fingerprint fingerprint, List<SetSessionFileReader> readers) {
		try {
			File setFile = fileOf(fingerprint);
			File tempFile = union(fingerprint, readers);
			Files.move(tempFile.toPath(), setFile.toPath(), REPLACE_EXISTING);
		} catch (IOException e) {
			Logger.error(e);
		}
	}

	private File union(Fingerprint fingerprint, List<SetSessionFileReader> readers) throws IOException {
		File tempFile = File.createTempFile(fingerprint.toString(), SetExtension, tempFolder);
		new ZetWriter(tempFile).write(new ZetStream.Union(zetStreamsOf(fingerprint, readers)));
		return tempFile;
	}

	private List<ZetStream> zetStreamsOf(Fingerprint fingerprint, List<SetSessionFileReader> readers) {
		List<ZetStream> streams = collectZetStreams(fingerprint, readers);
		File setFile = fileOf(fingerprint);
		if (setFile.exists()) streams.add(new ZetReader(setFile));
		return streams;
	}


	private List<ZetStream> collectZetStreams(Fingerprint fingerprint, List<SetSessionFileReader> readers) {
		return readers.stream()
				.map(r -> r.chunks(fingerprint))
				.flatMap(r -> r)
				.map(SetSessionFileReader.Chunk::stream)
				.collect(toList());
	}

	private Stream<File> setBlobs() {
		return files.stream().filter(f -> f.getName().endsWith(extensionOf(Blob.Type.set)));
	}

	private File fileOf(Fingerprint fingerprint) {
		File file = new File(setStoreFolder, fingerprint + SetExtension);
		file.getParentFile().mkdirs();
		return file;
	}

	private Set<Fingerprint> fingerPrintsIn(List<SetSessionFileReader> readers) {
		return readers.stream()
				.map(SetSessionFileReader::chunks)
				.flatMap(r -> r)
				.map(SetSessionFileReader.Chunk::fingerprint)
				.collect(Collectors.toSet());
	}

}
