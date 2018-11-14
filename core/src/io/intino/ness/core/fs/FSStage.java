package io.intino.ness.core.fs;

import io.intino.alexandria.logger.Logger;
import io.intino.ness.core.Blob;
import io.intino.ness.core.Stage;

import java.io.*;
import java.util.stream.Stream;

import static java.util.Arrays.stream;
import static java.util.stream.Stream.empty;

public class FSStage implements Stage {
	private static final String BlobExtension = ".blob";
	private final File root;

	public FSStage(File root) {
		this.root = root;
		this.root.mkdirs();
	}

	private static String extensionOf(Blob.Type type) {
		return "." + type.name() + BlobExtension;
	}

	@Override
	public OutputStream start(Blob.Type type) {
		return start("", type);
	}

	@Override
	public OutputStream start(String prefix, Blob.Type type) {
		try {
			return new FileOutputStream(fileOf(prefix, type));
		} catch (IOException e) {
			return null;
		}
	}

	public Stream<Blob> blobs() {
		return files().map(FileBlob::new);
	}

	@Override
	public void clear() {
		files().forEach(File::delete);
	}

	private Stream<File> files() {
		File[] files = root.listFiles(this::blobs);
		return files == null ? empty() : stream(files);
	}

	private File fileOf(String name, Blob.Type type) {
		return new File(root, withExtension(name(name), type));
	}

	private String withExtension(String name, Blob.Type type) {
		return name + extensionOf(type);
	}

	private boolean blobs(File dir, String name) {
		return name.endsWith(BlobExtension);
	}

	private static class FileBlob implements Blob {

		private final File file;
		private final Type type;

		public FileBlob(File file) {
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
