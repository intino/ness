package io.intino.ness.core.local.sessions;

import io.intino.alexandria.logger.Logger;
import io.intino.alexandria.zim.ZimBuilder;
import io.intino.alexandria.zim.ZimReader;
import io.intino.alexandria.zim.ZimStream;
import io.intino.ness.core.local.FS;
import io.intino.ness.core.local.LocalEventStore;
import io.intino.ness.core.sessions.Fingerprint;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.intino.ness.core.local.LocalEventStore.SessionExtension;
import static java.util.Collections.singletonList;

public class EventSessionManager {

	public static void seal(File stageFolder, File eventStoreFolder, File tempFolder) {
		eventSessions(stageFolder)
				.collect(Collectors.groupingBy(EventSessionManager::fingerprintOf)).entrySet()
				.stream().sorted(Comparator.comparing(t -> t.getKey().toString()))
				.parallel().forEach(e -> new Sealer(eventStoreFolder, tempFolder).seal(e.getKey(), e.getValue()));
	}

	private static Stream<File> eventSessions(File eventStageFolder) {
		return FS.filesIn(eventStageFolder, f -> f.getName().endsWith(SessionExtension) && f.length() > 0f);
	}

	private static ZimReader reader(File zimFile) {
		return new ZimReader(zimFile);
	}

	private static Fingerprint fingerprintOf(File file) {
		return new Fingerprint(cleanedNameOf(file));
	}

	private static String cleanedNameOf(File file) {
		return file.getName().substring(0, file.getName().indexOf("#")).replace("-", "/").replace(SessionExtension, "");
	}

	private static class Sealer {
		private final File eventStoreFolder;
		private final File tempFolder;

		Sealer(File eventStoreFolder, File tempFolder) {
			this.eventStoreFolder = eventStoreFolder;
			this.tempFolder = tempFolder;
		}

		public void seal(Fingerprint fingerprint, List<File> files) {
			for (File file : files) seal(datalakeFile(fingerprint), singletonList(sort(file)));
		}

		private void seal(File datalakeFile, List<File> files) {
			new ZimBuilder(datalakeFile).put(zimStreamOf(files));
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

		private ZimStream.Merge zimStreamOf(List<File> files) {
			return ZimStream.Merge.of(files.stream().map(EventSessionManager::reader).toArray(ZimStream[]::new));
		}

		private File datalakeFile(Fingerprint fingerprint) {
			File zimFile = new File(eventStoreFolder, fingerprint.toString() + LocalEventStore.EventExtension);
			zimFile.getParentFile().mkdirs();
			return zimFile;
		}
	}
}
