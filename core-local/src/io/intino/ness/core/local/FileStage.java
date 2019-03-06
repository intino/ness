package io.intino.ness.core.local;

import io.intino.alexandria.logger.Logger;
import io.intino.ness.core.Session;
import io.intino.ness.core.Stage;

import java.io.*;
import java.nio.file.Files;
import java.util.stream.Stream;

import static io.intino.ness.core.Session.Type.event;
import static io.intino.ness.core.local.FS.copyInto;
import static io.intino.ness.core.sessions.SessionHandler.SessionExtension;
import static java.time.Instant.now;
import static java.util.Arrays.stream;

class FileStage implements Stage {
	private final File stageFolder;
	private final File sessionsFolder;

	FileStage(File stageFolder, File sessionsFolder) {
		this.stageFolder = stageFolder;
		this.sessionsFolder = sessionsFolder;
		this.stageFolder.mkdirs();
		this.sessionsFolder.mkdirs();
	}

	@Override
	public void push(Stream<Session> sessions) {
		sessions.forEach(s -> push(s, stageFolder));
	}

	public void clear() {
		File treatedFolder = new File(sessionsFolder, sealDateFolderName());
		treatedFolder.mkdirs();
		FS.filesIn(stageFolder, File::isFile).forEach(f -> move(f, treatedFolder));
	}

	private void push(Session session, File stageFolder) {
		copyInto(fileFor(session, stageFolder), session.inputStream());
	}

	private File fileFor(Session session, File stageFolder) {
		return new File(stageFolder, filename(session));
	}

	private String filename(Session session) {
		return session.name() + (session.type() == event ? LocalEventStore.SessionExtension : SessionExtension);
	}

	private void move(File stageFile, File treatedFolder) {
		try {
			Files.move(stageFile.toPath(), new File(treatedFolder, stageFile.getName()).toPath());
		} catch (IOException e) {
			Logger.error(e);
		}
	}

	public Stream<Session> sessions() {
		return files().map(FileSession::new);
	}

	private String sealDateFolderName() {
		return now().toString().substring(0, 19).replaceAll("[:T\\-]", "");
	}

	private Stream<File> files() {
		return FS.allFilesIn(stageFolder, this::sessions);
	}

	private boolean sessions(File file) {
		return file.isDirectory() || file.getName().endsWith(SessionExtension);
	}

	private static class FileSession implements Session {

		private final File file;
		private final Type type;

		FileSession(File file) {
			this.file = file;
			this.type = typeOf(file.getName());
		}

		@Override
		public String name() {
			String name = file.getName();
			return name.substring(0, name.lastIndexOf("."));
		}

		private Type typeOf(String filename) {
			return stream(Type.values())
					.filter(type -> filename.endsWith(extensionOf(type)))
					.findFirst()
					.orElse(null);
		}

		private String extensionOf(Session.Type type) {
			return "." + type.name() + SessionExtension;
		}

		@Override
		public Type type() {
			return type;
		}

		@Override
		public InputStream inputStream() {
			return new BufferedInputStream(inputStreamOfFile());
		}

		private InputStream inputStreamOfFile() {
			try {
				return new FileInputStream(file);
			} catch (FileNotFoundException e) {
				Logger.error(e);
				return new ByteArrayInputStream(new byte[0]);
			}
		}

	}
}
