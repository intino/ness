package io.intino.ness.datalake;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.Stream;

public class FS {
	private File root;

	public FS(File root) {
		this.root = root;
	}

	public static Stream<File> foldersIn(File folder) {
		return Arrays.stream(new FS(folder).foldersIn(File::isDirectory, Sort.Normal));
	}

	public static Stream<File> foldersIn(File folder, Sort sort) {
		return Arrays.stream(new FS(folder).foldersIn(File::isDirectory, sort));
	}

	public static Stream<File> filesIn(File folder, FileFilter filter) {
		return Arrays.stream(new FS(folder).foldersIn(filter, Sort.Normal));
	}

	private File[] foldersIn(FileFilter filter, Sort sort) {
		File[] files = root.listFiles(filter);
		files = files == null ? new File[0] : files;
		Arrays.sort(files, sort.comparator);
		return files;
	}

	public enum Sort {
		Normal((x, y) -> x.getName().compareTo(y.getName())),
		Reversed((x, y) -> y.getName().compareTo(x.getName()));

		private final Comparator<File> comparator;

		Sort(Comparator<File> comparator) {
			this.comparator = comparator;
		}
	}
}
