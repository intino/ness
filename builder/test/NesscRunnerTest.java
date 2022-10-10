import io.intino.builder.NessRunner;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;


public class NesscRunnerTest {
	private String home;

	@Before
	public void setUp() {
		home = new File("test-res").getAbsolutePath() + File.separator;
	}

	private static String temp(String filepath) {
		try {
			File file = new File(filepath);
			String home = System.getProperty("user.home");
			String text = Files.readString(file.toPath()).replace("$WORKSPACE", home + File.separator + "workspace").replace("$HOME", home);
			Path temporalFile = Files.createTempFile(file.getName(), ".txt");
			Files.writeString(temporalFile, text, StandardOpenOption.TRUNCATE_EXISTING);
			temporalFile.toFile().deleteOnExit();
			return temporalFile.toFile().getAbsolutePath();
		} catch (IOException e) {
			return null;
		}
	}

	@Test
	public void cinepolisM1() {
//		FileSystemUtils.removeDir("C:\\Users\\naits\\Desktop\\IntinoDev\\master\\test\\gen\\com\\cinepolis\\master\\model");
		NessRunner.main(new String[]{temp(home + "cinepolis.txt")});
	}



	@Test
	public void testM1() {
//		FileSystemUtils.removeDir("C:\\Users\\naits\\Desktop\\IntinoDev\\master\\test\\gen\\com\\cinepolis\\master\\model");
		NessRunner.main(new String[]{temp(home + "test.txt")});
	}
}
