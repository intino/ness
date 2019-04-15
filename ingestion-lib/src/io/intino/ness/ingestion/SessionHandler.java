package io.intino.ness.ingestion;

import io.intino.alexandria.logger.Logger;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.UUID.randomUUID;

public class SessionHandler {
	public static final String SessionExtension = ".session";
	private final File root;
	private List<PrivateSession> sessions = new ArrayList<>();

	public SessionHandler() {
		this.root = null;
	}

	public SessionHandler(File root) {
		this.root = root;
	}

	public SetSession createSetSession() {
		PrivateProvider provider = new PrivateProvider();
		return new SetSession(provider, new SetSessionFileWriter(provider.outputStream(Session.Type.set)));
	}

	public SetSession createSetSession(int autoFlushSize) {
		PrivateProvider provider = new PrivateProvider();
		return new SetSession(provider, new SetSessionFileWriter(provider.outputStream(Session.Type.set)), autoFlushSize);
	}

	public EventSession createEventSession() {
		return new EventSession(new PrivateProvider());
	}

	public void clear() {
		sessions.clear();
	}

	public Stream<Session> sessions() {
		return (sessions.isEmpty() ? loadFileSessions() : sessions).stream()
				.map(s -> new Session() {
					@Override
					public String name() {
						return s.name() + "." + s.type();
					}

					@Override
					public Type type() {
						return s.type();
					}

					@Override
					public InputStream inputStream() {
						return s.inputStream();
					}
				});
	}

	private List<PrivateSession> loadFileSessions() {
		return sessionFiles()
				.map(f -> new PrivateSession(name(f), typeOf(f), new FileSessionData(f))).collect(Collectors.toList());
	}

	private Stream<File> sessionFiles() {
		try {
			if (this.root == null) return Stream.empty();
			return Files.walk(root.toPath())
					.filter(path -> Files.isRegularFile(path) && path.toFile().getName().endsWith(SessionExtension))
					.map(Path::toFile);
		} catch (IOException e) {
			Logger.error(e);
			return Stream.empty();
		}
	}

	private String name(File f) {
		return f.getName().substring(0, f.getName().indexOf("#"));
	}

	private Session.Type typeOf(File f) {
		String[] split = f.getName().split("\\.");
		return Session.Type.valueOf(split[split.length - 2]);
	}

	private interface SessionData {
		InputStream inputStream();

		OutputStream outputStream();

	}

	public interface Provider {
		OutputStream outputStream(Session.Type type);

		OutputStream outputStream(String name, Session.Type type);
	}

	private class PrivateProvider implements Provider {

		public OutputStream outputStream(Session.Type type) {
			return outputStream("", type);
		}

		public OutputStream outputStream(String name, Session.Type type) {
			PrivateSession session = session(name + suffix(), type);
			sessions.add(session);
			return session.outputStream();
		}

		private PrivateSession session(String name, Session.Type type) {
			return new PrivateSession(name, type, root == null ? new MemorySessionData() : new FileSessionData(fileOf(name, type)));
		}

		private File fileOf(String name, Session.Type type) {
			return new File(root, filename(name, type));
		}

		private String filename(String name, Session.Type type) {
			return name + suffix() + extensionOf(type);
		}

		private String suffix() {
			return "#" + randomUUID().toString();
		}

		private String extensionOf(Session.Type type) {
			return "." + type.name() + SessionExtension;
		}
	}

	private class PrivateSession {
		private final String name;
		private final Session.Type type;
		private SessionData sessionData;


		public PrivateSession(String name, Session.Type type, SessionData sessionData) {
			this.name = name;
			this.type = type;
			this.sessionData = sessionData;
		}

		public String name() {
			return name;
		}

		public Session.Type type() {
			return type;
		}


		public InputStream inputStream() {
			return sessionData.inputStream();
		}

		public OutputStream outputStream() {
			return sessionData.outputStream();
		}

	}

	private class FileSessionData implements SessionData {
		private final File file;

		public FileSessionData(File file) {
			this.file = file;
		}

		@Override
		public InputStream inputStream() {
			try {
				return new FileInputStream(file);
			} catch (FileNotFoundException e) {
				Logger.error(e);
				return null;
			}
		}

		@Override
		public OutputStream outputStream() {
			try {
				return new BufferedOutputStream(new FileOutputStream(file));
			} catch (FileNotFoundException e) {
				Logger.error(e);
				return null;
			}
		}
	}

	private class MemorySessionData implements SessionData {

		private final ByteArrayOutputStream outputStream;

		public MemorySessionData() {
			this.outputStream = new ByteArrayOutputStream();
		}

		@Override
		public InputStream inputStream() {
			return new ByteArrayInputStream(outputStream.toByteArray());
		}

		@Override
		public OutputStream outputStream() {
			return outputStream;
		}
	}
}
