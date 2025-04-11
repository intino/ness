package systems.intino.eventsourcing.datahubbuilder.codegeneration;


import io.intino.alexandria.logger.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Commons {
	public static void writeFile(File packageFolder, String name, String text) {
		try {
			packageFolder.mkdirs();
			File file = javaFile(packageFolder, name);
			Files.writeString(file.toPath(), text);
		} catch (IOException e) {
			Logger.error(e);
		}
	}

	public static void write(File file, String text) {
		write(file.toPath(), text);
	}

	public static void write(Path file, String text) {
		try {
			Files.writeString(file, text);
		} catch (IOException e) {
			Logger.error(e);

		}
	}

	public static File javaFile(File packageFolder, String name) {
		return preparedFile(packageFolder, name, "java");
	}


	public static String firstUpperCase(String value) {
		return value.isEmpty() ? "" : value.substring(0, 1).toUpperCase() + value.substring(1);
	}

	private static File preparedFile(File packageFolder, String name, String extension) {
		return new File(packageFolder, prepareName(name) + "." + extension);
	}

	private static String prepareName(String name) {
		return name.isEmpty() ? name : Character.toUpperCase(name.charAt(0)) + name.substring(1);
	}

}
