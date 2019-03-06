package io.intino.ness.core.remote.sessions;

import io.intino.alexandria.logger.Logger;
import io.intino.alexandria.zim.ZimReader;
import io.intino.alexandria.zim.ZimStream;
import io.intino.ness.core.Datalake;
import io.intino.ness.core.Session;
import io.intino.ness.core.Fingerprint;
import io.intino.ness.core.remote.Paths;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.intino.ness.core.remote.Paths.stagePath;
import static io.intino.ness.core.remote.Paths.tempPath;
import static io.intino.ness.core.remote.RemoteEventStore.EventExtension;
import static io.intino.ness.core.remote.RemoteEventStore.SessionExtension;
import static java.util.Collections.singletonList;

public class EventSessionManager {

	private final FileSystem fs;
	private final Path root;

	public EventSessionManager(FileSystem fs, Path root) {
		this.fs = fs;
		this.root = root;
	}


	public void seal() {
		try {
			eventSessionBlobs()
					.collect(Collectors.groupingBy((File file) -> fingerprintOf(file))).entrySet()
					.stream().sorted(Comparator.comparing(t -> t.getKey().toString()))
					.parallel().forEach(e -> new Sealer(Paths.eventStorePath(root), tempPath(root)).seal(e.getKey(), e.getValue()));
		} catch (IOException e) {
			Logger.error(e);
		}
	}

	private Stream<FileStatus> eventSessionBlobs() throws IOException {
		Arrays.stream(fs.listStatus(stagePath(root), this::sessions));
	}

	private boolean sessions(Path f) {
		try {
			return f.getName().endsWith(SessionExtension) && fs.getFileStatus(f).getLen() > 0f;
		} catch (IOException e) {
			Logger.error(e);
			return false;
		}
	}

	private Path pathFor(Session session) {
		return new Path(stagePath(root), filename(session));
	}

	private String filename(Session session) {
		return session.name() + SessionExtension;
	}

	private Fingerprint fingerprintOf(File file) {
		return new Fingerprint(cleanedNameOf(file));
	}

	private String cleanedNameOf(File file) {
		return file.getName().substring(0, file.getName().indexOf("#")).replace("-", "/").replace(SessionExtension, "");
	}

	private class Sealer {

		private final Path eventStorePath;
		private final Path tempPath;

		Sealer(Path eventStorePath, Path tempPath) {
			this.eventStorePath = eventStorePath;
			this.tempPath = tempPath;
		}

		public void seal(Fingerprint fingerprint, List<Path> paths) throws IOException {
			for (Path path : paths) seal(datalakePath(fingerprint), singletonList(sort(path)));
		}

		private Path sort(Path path) {
//			try {
//				new EventSorter(path, tempPath).sort();
//				return path;
//			} catch (IOException e) {
//				Logger.error(e);
//				return null;
//			}TODO
			return null;
		}

		private void seal(Path destination, List<Path> files) {
//			new ZimBuilder(destination).put(zimStreamOf(files));
			//TODO
		}

		private ZimStream.Merge zimStreamOf(List<Path> files) {
			return ZimStream.Merge.of(files.stream().map(this::reader).toArray(ZimStream[]::new));
		}

		private Path datalakePath(Fingerprint fingerprint) throws IOException {
			Path zimFile = new Path(eventStorePath, fingerprint.toString() + EventExtension);
			fs.mkdirs(zimFile.getParent());
			return zimFile;
		}

		private ZimReader reader(Path zimFile) {
//			return new ZimReader(zimFile);
			return null;//TODO
		}
	}
}
