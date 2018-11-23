package io.intino.ness.core.sessions;

import io.intino.alexandria.zim.ZimBuilder;
import io.intino.alexandria.zim.ZimReader;
import io.intino.ness.core.Blob;
import io.intino.ness.core.fs.FS;
import io.intino.ness.core.sessions.sorters.MessageSorter;

import java.io.File;
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

	public static void seal(File eventStageFolder, File eventStoreFolder) {
		eventSessionBlobs(eventStageFolder).parallel()
				.forEach(f -> new Sealer(eventStoreFolder).seal(f));
	}

	private static class Sealer {

		private File eventStoreFolder;

		public Sealer(File eventStoreFolder) {
			this.eventStoreFolder = eventStoreFolder;
		}

		public void seal(File file) {
			seal(datalakeFile(file), sessionFile(file));
		}

		private File sessionFile(File file) {
			return sort(file);
		}

		private void seal(File datalakeFile, File sessionFile) {
			new ZimBuilder(datalakeFile).put(reader(sessionFile));
		}

		private File datalakeFile(File file) {
			File zimFile = new File(eventStoreFolder, fingerprintOf(file).toString() + EventExtension);
			zimFile.getParentFile().mkdirs();
			return zimFile;
		}

		private Fingerprint fingerprintOf(File file) {
			return new Fingerprint(cleanedNameOf(file));
		}

		private String cleanedNameOf(File file) {
			return file.getName().substring(0, file.getName().indexOf("#")).replace("-", "/").replace(SessionExtension, "");
		}

		private static File sort(File file) {
			return new MessageSorter(file).sort();
		}

	}

	private static Stream<File> eventSessionBlobs(File eventStageFolder) {
		return FS.filesIn(eventStageFolder, f -> f.getName().endsWith(SessionExtension));
	}

	private static ZimReader reader(File zimFile) {
		return new ZimReader(zimFile);
	}

}
