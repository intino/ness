package io.intino.ness.ingestion;

import io.intino.alexandria.logger.Logger;
import io.intino.alexandria.nessaccessor.local.LocalDatalake;
import io.intino.ness.datalake.file.FileDatalake;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;

public class Seal_ {


	@Test
	public void should_create_a_session() throws IOException {
		System.out.println(Instant.now());
		FileSessionManager fileSessionManager = new FileSessionManager(new FileDatalake(new File("../temp/datalake")), new File("../temp/session"));
		fileSessionManager.seal();
	}


	@Test
	public void old_seal_session() throws IOException {
		Logger.info("Seal ignited! " + LocalDateTime.now().toString());
		new LocalDatalake(new File("../temp/datalake")).seal();
		Logger.info("Seal finished! " + LocalDateTime.now().toString());
	}
}
