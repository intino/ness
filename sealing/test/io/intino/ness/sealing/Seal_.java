package io.intino.ness.sealing;

import io.intino.ness.datalake.file.FileDatalake;
import org.junit.Test;

import java.io.File;
import java.time.Instant;

public class Seal_ {

	private static final String SESSIONS_FOLDER = "../temp/session";
	private static final File STAGE_FOLDER = new File("../temp/stage");
	private File DATALAKE = new File("../temp/datalake");

	@Test
	public void should_create_a_session() {
		System.out.println(Instant.now());
		FileSessionManager fileSessionManager = new FileSessionManager(new FileDatalake(DATALAKE), new File(SESSIONS_FOLDER), STAGE_FOLDER);
		fileSessionManager.seal();
	}
}