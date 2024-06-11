package io.intino.ness.builder.test;

import io.intino.alexandria.logger.Logger;
import io.intino.ness.builder.NesscRunner;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class TerminalRunnerTest {
	private String home;

	@Before
	public void setUp() {
		home = new File("test-res").getAbsolutePath() + File.separator;
	}

	@Test
	@Ignore
	public void example() {
		NesscRunner.main(new String[]{temp(home + "example.txt")});
	}

	private static String temp(String filepath) {
		try {
			File file = new File(filepath);
			String home = System.getProperty("user.home");
			String text = Files.readString(file.toPath()).replace("$WORKSPACE", configurationWorkspace(home)).replace("$HOME", home);
			Path temporalFile = Files.createTempFile(file.getName(), ".txt");
			Files.writeString(temporalFile, text, StandardOpenOption.TRUNCATE_EXISTING);
			return temporalFile.toFile().getAbsolutePath();
		} catch (IOException e) {
			return null;
		}
	}

	private static String configurationWorkspace(String home) {
		try {
			InputStream stream = TerminalRunnerTest.class.getResourceAsStream("/workspace.txt");
			if (stream == null) return home;
			return new String(stream.readAllBytes());
		} catch (IOException e) {
			Logger.error(e);
			return home;
		}
	}
}
