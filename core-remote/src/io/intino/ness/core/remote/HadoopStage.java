package io.intino.ness.core.remote;

import io.intino.alexandria.logger.Logger;
import io.intino.ness.core.Session;
import io.intino.ness.core.Stage;
import io.intino.ness.core.remote.sessions.SessionWriter;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.stream.Stream;

import static io.intino.ness.core.remote.RemoteEventStore.SessionExtension;
import static java.time.Instant.now;
import static java.util.Arrays.stream;

class HadoopStage implements Stage {
	private final FileSystem fs;
	private final Path path;
	private final Path sessions;

	HadoopStage(FileSystem fs, Path path, Path sessions) {
		this.fs = fs;
		this.path = path;
		this.sessions = sessions;
	}

	public Path path() {
		return path;
	}

	public Stream<Session> sessions() {
		return Stream.of(new HadoopSession(path));
	}

	@Override
	public void push(Stream<Session> sessions) {
		SessionWriter writer = new SessionWriter(fs, path);
		sessions.forEach(writer::write);
	}

	@Override
	public void clear() {
		try {
			Path destination = new Path(sessions, sealDateFolderName());
			Arrays.stream(fs.listStatus(destination, this::isFile)).forEach(f -> move(f, destination));
		} catch (IOException e) {
			Logger.error(e);
		}
	}

	private void move(FileStatus f, Path destination) {
		try {
			fs.rename(f.getPath(), destination);
		} catch (IOException e) {
			Logger.error(e);
		}
	}

	private boolean isFile(Path l) {
		try {
			return !fs.isDirectory(l);
		} catch (IOException e) {
			Logger.error(e);
			return false;
		}
	}

	private String sealDateFolderName() {
		return now().toString().substring(0, 19).replaceAll("[:T\\-]", "");
	}

	private String extensionOf(Session.Type type) {
		return "." + type.name() + SessionExtension;
	}

	private class HadoopSession implements Session {
		private final Path path;
		private final Type type;

		HadoopSession(Path path) {
			this.path = path;
			this.type = typeOf(path.getName());
		}

		@Override
		public String name() {
			String name = path.getName();
			return name.substring(0, name.lastIndexOf("."));
		}

		private Type typeOf(String filename) {
			return stream(Type.values())
					.filter(type -> filename.endsWith(extensionOf(type)))
					.findFirst()
					.orElse(null);
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
				return fs.open(path);
			} catch (IOException e) {
				Logger.error(e);
				return new ByteArrayInputStream(new byte[0]);
			}
		}
	}
}