package io.intino.ness.ingestion;

import io.intino.ness.datalake.file.FileDatalake;
import org.junit.Test;

import java.io.File;
import java.time.Instant;

public class Seal_ {
	@Test
	public void should_create_a_session() {
		System.out.println(Instant.now());
		FileSessionManager fileSessionManager = new FileSessionManager(new FileDatalake(new File("../temp/datalake")), new File("../temp/session"));
		fileSessionManager.seal();
	}
}