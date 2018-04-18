package io.intino.ness.datalake;

import io.intino.ness.inl.Message;
import io.intino.ness.inl.streams.FileMessageOutputStream;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.UUID;

import static io.intino.ness.datalake.FileTank.Format.zip;
import static io.intino.ness.inl.Message.empty;
import static java.util.Arrays.stream;

public class FileTank implements Tank {

	private final File folder;
	private final Writer writer;

	public FileTank(File folder) {
		this.folder = folder;
		this.writer = new Writer();
	}

	private static String dayOf(String instant) {
		return instant.replace("-", "").substring(0, 8);
	}

	@Override
	public String name() {
		return folder.getName();
	}

	@Override
	public Tub[] tubs() {
		return stream(files())
				.sorted(File::compareTo)
				.map(this::tub)
				.toArray(Tub[]::new);
	}

	private Tub tub(File file) {
		return new FileTub(file);
	}

	@Override
	public Tub get(Instant instant) {
		return new FileTub(fileOf(instant, zip));
	}


	private File[] files() {
		create();
		File[] files = folder.listFiles(this::isTub);
		return files != null ? files : new File[0];
	}

	private boolean isTub(File file) {
		return isZip(file) || (isInl(file) && !sealFile(file).exists());
	}

	private boolean isInl(File file) {
		return file.getName().endsWith("inl");
	}

	private boolean isZip(File file) {
		return file.getName().endsWith(".zip");
	}

	private File sealFile(File file) {
		return new File(file.getAbsolutePath().replace(".inl", ".zip"));
	}

	File[] files(Format format) {
		create();
		return folder.listFiles(f -> f.getName().endsWith(format.name()));
	}

	File fileOf(Message message, Format format) {
		create();
		return new File(folder, dayOf(message.ts()) + "." + format.name());
	}

	File fileOf(Instant instant, Format format) {
		create();
		return new File(folder, dayOf(instant.toString()) + "." + format.name());
	}

	Tank create() {
		if(!folder.exists()) folder.mkdirs();
		return this;
	}

	void rename(String newName) {
		create();
		if (rename(folder, newName)) return;
		throw new RuntimeException("Tank could not be renamed");
	}

	void remove() {
		if (isEmpty()) {
			if (folder.delete()) return;
			throw new RuntimeException("Empty tank could not be removed");
		}
		if (rename(folder, "trash." + uid() + "." + folder.getName())) return;
		throw new RuntimeException("Tank could not be removed");

	}

	private String uid() {
		return UUID.randomUUID().toString();
	}

	private boolean rename(File file, String newName) {
		return file.renameTo(new File(file.getParent(), newName));
	}

	boolean isEmpty() {
		create();
		return folder.listFiles() == null;
	}

	void write(Message message) {
		if (message == empty) return;
		try {
			this.writer.write(message);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	void close() {
		try {
			this.writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public enum Format {
		zip, inl
	}

	private class Writer {
		private File file;
		private FileMessageOutputStream os = null;

		public void write(Message message) throws IOException {
			open(message);
			os.write(message);
		}

		private void open(Message message) throws IOException {
			File file = fileOf(message, zip);
			if (file.equals(this.file)) return;
			close();
			this.file = file;
			this.os = FileMessageOutputStream.of(file);
		}

		public void close() throws IOException {
			if (this.os == null) return;
			try {
				this.os.close();
			} finally {
				this.os = null;
			}
		}

	}


}