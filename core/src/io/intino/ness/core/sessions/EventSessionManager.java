package io.intino.ness.core.sessions;

import io.intino.alexandria.logger.Logger;
import io.intino.alexandria.zim.ZimBuilder;
import io.intino.alexandria.zim.ZimReader;
import io.intino.alexandria.zim.ZimStream;
import io.intino.ness.core.Blob;
import io.intino.ness.core.fs.FS;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.intino.ness.core.fs.FS.copyInto;
import static io.intino.ness.core.fs.FSDatalake.BlobExtension;
import static io.intino.ness.core.fs.FSEventStore.EventExtension;
import static io.intino.ness.core.fs.FSEventStore.SessionExtension;

public class EventSessionManager {

	public static void push(File stageFolder, Blob blob) {
		copyInto(fileFor(blob, stageFolder), blob.inputStream());
	}

	private static File fileFor(Blob blob, File stageFolder) {
		return new File(stageFolder, filename(blob));
	}

	private static String filename(Blob blob) {
		return blob.name() + BlobExtension;
	}

	public static void seal(File stageFolder, File eventStoreFolder, File tempFolder) {
		eventSessionBlobs(stageFolder)
				.collect(Collectors.groupingBy(Sealer::fingerprintOf)).entrySet()
				.parallelStream().sorted(Comparator.comparing(t -> t.getKey().toString()))
				.forEach(e -> new Sealer(eventStoreFolder, tempFolder).seal(e.getKey(), e.getValue()));
	}

	private static Stream<File> eventSessionBlobs(File eventStageFolder) {
		return FS.filesIn(eventStageFolder, f -> f.getName().endsWith(SessionExtension) && f.length() > 0f);
	}

	private static ZimReader reader(File zimFile) {
		return new ZimReader(zimFile);
	}

	private static class Sealer {
		private final File eventStoreFolder;
		private final File tempFolder;

		public Sealer(File eventStoreFolder, File tempFolder) {
			this.eventStoreFolder = eventStoreFolder;
			this.tempFolder = tempFolder;
		}

		private File sort(File file) {
			try {
				new EventSorter(file, tempFolder).sort();
				return file;
			} catch (IOException e) {
				Logger.error(e);
				return null;
			}
		}

		private static Fingerprint fingerprintOf(File file) {
			return new Fingerprint(cleanedNameOf(file));
		}

		private static String cleanedNameOf(File file) {
			return file.getName().substring(0, file.getName().indexOf("#")).replace("-", "/").replace(SessionExtension, "");
		}

		public void seal(Fingerprint fingerprint, List<File> files) {
			seal(datalakeFile(fingerprint), files);
			//FIXME seal(datalakeFile(file), sort(file));  asemed culpable
		}

		private void seal(File datalakeFile, List<File> files) {
			new ZimBuilder(datalakeFile).put(zimStreamOf(files));
		}

		private ZimStream.Merge zimStreamOf(List<File> files) {
			return ZimStream.Merge.of(files.stream().map(EventSessionManager::reader).toArray(ZimStream[]::new));
		}

		private File datalakeFile(Fingerprint fingerprint) {
			File zimFile = new File(eventStoreFolder, fingerprint.toString() + EventExtension);
			zimFile.getParentFile().mkdirs();
			return zimFile;
		}
	}

}
