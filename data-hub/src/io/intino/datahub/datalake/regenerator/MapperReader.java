package io.intino.datahub.datalake.regenerator;

import io.intino.alexandria.logger.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class MapperReader {

	public static final String JAVA = ".java";
	private final File mappersDirectory;

	public MapperReader(File mappersDirectory) {
		this.mappersDirectory = mappersDirectory;
	}

	public String read(String mapperName) {
		File mapperFile = new File(mappersDirectory, mapperName + JAVA);
		if (!mapperFile.exists()) return null;
		try {
			return Files.readString(mapperFile.toPath());
		} catch (IOException e) {
			Logger.error(e);
			return null;
		}
	}
}
