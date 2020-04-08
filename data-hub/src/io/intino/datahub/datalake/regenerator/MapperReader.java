package io.intino.datahub.datalake.regenerator;

import io.intino.alexandria.logger.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;

public class MapperReader {

	public static final String JAVA = ".java";
	private final File mappersDirectory;

	public MapperReader(File mappersDirectory) {
		this.mappersDirectory = mappersDirectory;
	}

	public String read(String mapperName) {
		File[] files = mappersDirectory.listFiles(f -> f.getName().equals(JAVA));
		if (files != null) {
			File mapperFile = Arrays.stream(files).filter(f -> f.getName().equals(mapperName + JAVA)).findFirst().orElse(null);
			if (mapperFile == null) return null;
			try {
				return Files.readString(mapperFile.toPath());
			} catch (IOException e) {
				Logger.error(e);
				return null;
			}
		} else return null;
	}
}
