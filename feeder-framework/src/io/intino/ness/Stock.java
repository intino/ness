package io.intino.ness;

import io.intino.ness.inl.MessageInputStream;
import io.intino.ness.inl.streams.FileMessageInputStream;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Arrays.stream;

public class Stock {

	private final File folder;
	private Joint joint;

	public Stock(File folder, Joint joint) {
		this.folder = folder;
		this.joint = joint;
	}

	private static String dayOf(String instant) {
		return instant.replace("-", "").substring(0, 8);
	}

	public Bundle[] bundles() {
		return stream(files())
				.sorted(File::compareTo)
				.map(this::bundle)
				.toArray(Bundle[]::new);
	}

	private Bundle bundle(File file) {
		return file.isDirectory() ? new FolderBundle(file, joint) : new FileBundle(file);
	}

	private File[] files() {
		File[] files = folder.listFiles();
		return files != null ? files : new File[0];
	}


	public interface Bundle {
		MessageInputStream input();
	}

	public static class FolderBundle implements Bundle {

		private File folder;
		private Joint joint;

		public FolderBundle(File folder, Joint joint) {
			this.folder = folder;
			this.joint = joint;
		}

		public MessageInputStream input() {
			return joint.join(inputStreams());
		}

		private MessageInputStream[] inputStreams() {
			return stream(files())
					.map(this::inputStream)
					.toArray(MessageInputStream[]::new);
		}

		private MessageInputStream inputStream(File file) {
			try {
				return FileMessageInputStream.of(file);
			} catch (IOException e) {
				e.printStackTrace();
				return new MessageInputStream.Empty();
			}
		}

		private File[] files() {
			return folder.listFiles(Format::isAccepted);
		}

	}

	public static class FileBundle implements Bundle{

		private File file;

		public FileBundle(File file) {
			this.file = file;
		}

		public MessageInputStream input() {
			return inputStream(file);
		}

		private MessageInputStream inputStream(File file) {
			try {
				return FileMessageInputStream.of(file);
			} catch (IOException e) {
				e.printStackTrace();
				return new MessageInputStream.Empty();
			}
		}

		private File[] files() {
			return file.listFiles(Format::isAccepted);
		}

	}
	private static class Format {

		private static List<String> formats = asList("inl", "csv", "tsv", "dat");

		static boolean isAccepted(File file){
			return formats.contains(extensionOf(file.getName()));
		}

		private static String extensionOf(String name) {
			return name.contains(".") ? name.substring(name.lastIndexOf(".") + 1) : name;
		}
	}
}
