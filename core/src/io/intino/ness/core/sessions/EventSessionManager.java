package io.intino.ness.core.sessions;

import io.intino.alexandria.logger.Logger;
import io.intino.alexandria.zim.ZimBuilder;
import io.intino.alexandria.zim.ZimReader;
import io.intino.alexandria.zim.ZimStream;
import io.intino.alexandria.zim.ZimStream.Merge;
import io.intino.ness.core.Blob;
import io.intino.ness.core.fs.ExternalSorter;
import io.intino.ness.core.fs.FS;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static io.intino.ness.core.fs.FS.copyInto;
import static io.intino.ness.core.fs.FSEventStore.EventExtension;
import static io.intino.ness.core.fs.FSEventStore.SessionExtension;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class EventSessionManager {

	public static void push(File stageFolder, Blob blob) {
		copyInto(fileFor(blob, stageFolder), blob.inputStream());
	}

	private static File fileFor(Blob blob, File stageFolder) {
		return new File(stageFolder, blob.name() + SessionExtension);
	}

	public static void seal(File eventStageFolder, File eventStoreFolder) {
		FS.filesIn(eventStageFolder, f -> f.getName().endsWith(SessionExtension))
				.forEach(f -> merge(sort(f), zimFile(eventStoreFolder, f)));
	}

	private static void merge(File sessionFile, File zimFile) {
		ZimStream stream = !zimFile.exists() ? reader(sessionFile) : Merge.of(reader(sessionFile), reader(zimFile));
		File tempFile = tempFile();
		try {
			ZimBuilder builder = new ZimBuilder(tempFile);
			builder.put(stream);
			Files.move(tempFile.toPath(), zimFile.toPath(), REPLACE_EXISTING);
		} catch (IOException e) {
			Logger.error(e);
		}
	}

	private static File tempFile() {
		try {
			return File.createTempFile("merge", EventExtension);
		} catch (IOException e) {
			Logger.error(e);
			return new File("toMerge");
		}
	}

	private static ZimReader reader(File zimFile) {
		return new ZimReader(zimFile);
	}

	private static File zimFile(File eventStageFolder, File file) {
		File zimFile = new File(eventStageFolder, fingerprintOf(file).toString() + EventExtension);
		zimFile.getParentFile().mkdirs();
		return zimFile;
	}

	private static Fingerprint fingerprintOf(File file) {
		return new Fingerprint(cleanedNameOf(file));
	}

	private static String cleanedNameOf(File file) {
		return file.getName().substring(0, file.getName().indexOf("#")).replace("-", "/").replace(SessionExtension, "");
	}

	private static File sort(File file) {
		return new ExternalSorter(file).sort();
	}
}
