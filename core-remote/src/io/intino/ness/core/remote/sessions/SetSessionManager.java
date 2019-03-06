package io.intino.ness.core.remote.sessions;

import io.intino.alexandria.logger.Logger;
import io.intino.alexandria.triplestore.FileTripleStore;
import io.intino.alexandria.triplestore.MemoryTripleStore;
import io.intino.alexandria.triplestore.TripleStore;
import io.intino.alexandria.zet.ZetReader;
import io.intino.alexandria.zet.ZetStream;
import io.intino.alexandria.zet.ZetWriter;
import io.intino.ness.core.Fingerprint;
import io.intino.ness.core.Session;
import io.intino.ness.core.remote.Paths;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Stream;

import static io.intino.ness.core.remote.Paths.setStorePath;
import static io.intino.ness.core.remote.Paths.tempPath;
import static io.intino.ness.core.remote.RemoteEventStore.SessionExtension;
import static io.intino.ness.core.remote.RemoteSetStore.MetadataFilename;
import static io.intino.ness.core.remote.RemoteSetStore.SetExtension;

public class SetSessionManager {
	private final FileSystem fs;
	private final Path root;
	private final Path stage;
	private int count = 1;
	private int size;

	public SetSessionManager(FileSystem fs, Path root) {
		this.fs = fs;
		this.root = root;
		this.stage = Paths.stagePath(root);
	}


	private static String extensionOf(Session.Type type) {
		return "." + type.name() + SessionExtension;
	}

	public void push(Session session) {
		new SessionWriter(fs, stage).write(session);
	}

	public void seal() {
		sealStage();
		sealSetMetadataSessions();
	}

	private void sealStage() {
		try {
			StageReader stageReader = new StageReader(fs, stage);
			SetSessionReader readers = new SetSessionReader(stage);
			Set<Fingerprint> fingerprints = fingerPrintsIn(readers);
			size = fingerprints.size();
			fingerprints.parallelStream().forEach(fp -> {
				if (count % 10000 == 0) Logger.info(((count * 100.) / size) + "%");
				seal(fp, readers);
				count++;
			});
		} catch (IOException e) {
			Logger.error(e);
		}

	}

	private void sealSetMetadataSessions() {
		Map<Path, FileTripleStore.Builder> map = new HashMap<>();
		loadSetMetadataSessions()
				.flatMap(TripleStore::all)
				.forEach(s -> processTriple(s, map));
		map.values().forEach(FileTripleStore.Builder::close);
	}

	private void processTriple(String[] triple, Map<Path, TripleStore.Builder> map) {
		Fingerprint fingerprint = new Fingerprint(triple[0]);
		tripleStoreFor(metadataFileOf(fingerprint), map).put(fingerprint.set(), triple[1], triple[2]);
	}

	private TripleStore.Builder tripleStoreFor(Path path, Map<Path, TripleStore.Builder> map) {
		if (!map.containsKey(path)) {
			try {
				map.put(path, new FileTripleStore.Builder(fs.append(path)));
			} catch (IOException e) {
				Logger.error(e);
			}
		}
		return map.get(path);
	}

	private Path metadataFileOf(Fingerprint fingerprint) {
		try {
			Path path = new Path(setStorePath(root), fingerprint.tank() + "/" + fingerprint.timetag() + "/" + MetadataFilename);
			fs.mkdirs(path.getParent());
			return path;
		} catch (IOException e) {
			Logger.error(e);
			return null;
		}
	}

	private Stream<MemoryTripleStore> loadSetMetadataSessions() {
		return stage.parallelStream()
				.filter(f -> f.getPath().getName().endsWith(extensionOf(Session.Type.setMetadata)))
				.map(f -> new MemoryTripleStore(inputStreamOf(f.getPath())));
	}

	private InputStream inputStreamOf(Path path) {
		try {
			return new BufferedInputStream(fs.open(path));
		} catch (IOException e) {
			Logger.error(e);
			return null;
		}
	}

	private void seal(Fingerprint fingerprint, List<SetSessionReader> readers) {
		try {
			Path setFile = fileOf(fingerprint);
			Path tempFile = merge(fingerprint, readers);
			fs.rename(tempFile, setFile);
		} catch (IOException e) {
			Logger.error(e);
		}
	}

	private Path merge(Fingerprint fingerprint, List<SetSessionReader> readers) throws IOException {
		Path f = new Path(tempPath(root), fingerprint.toString() + SetExtension);
		FSDataOutputStream tempFile = fs.create(f);
		List<ZetStream> streams = zetStreamsOf(fingerprint, readers);
		new ZetWriter(tempFile).write(streams.size() == 1 ? streams.get(0) : new ZetStream.Merge(streams));
		return f;
	}

	private List<ZetStream> zetStreamsOf(Fingerprint fingerprint, List<SetSessionReader> readers) throws IOException {
		List<ZetStream> streams = collectZetStreams(fingerprint, readers);
		Path setFile = fileOf(fingerprint);
		if (fs.exists(setFile)) streams.add(new ZetReader(setFile));
		return streams;
	}


	private List<ZetStream> collectZetStreams(Fingerprint fingerprint, List<SetSessionReader> readers) {
		List<ZetStream> list = new ArrayList<>();
		for (SetSessionReader reader : readers) list.addAll(reader.streamsOf(fingerprint));
		return list;
	}

	private Stream<FileStatus> setBlobs() {
		return stage.stream().filter(f -> f.getPath().getName().endsWith(extensionOf(Session.Type.set)));
	}

	private Path fileOf(Fingerprint fingerprint) throws IOException {
		Path path = new Path(setStorePath(root), fingerprint + SetExtension);
		fs.mkdirs(path.getParent());
		return path;
	}

	private Set<Fingerprint> fingerPrintsIn(List<SetSessionReader> readers) {
		Set<Fingerprint> set = new HashSet<>();
		for (SetSessionReader reader : readers) set.addAll(reader.fingerprints());
		return set;
	}

}