import io.intino.alexandria.Scale;
import io.intino.alexandria.Timetag;
import io.intino.alexandria.mapp.MappReader;
import io.intino.alexandria.mapp.MappStream;
import io.intino.alexandria.zet.ZetStream;
import io.intino.ness.datalake.Datalake;
import io.intino.ness.datalake.hadoop.HadoopConnection;
import io.intino.ness.datalake.hadoop.HadoopDatalake;
import io.intino.ness.datalake.hadoop.HadoopSessionManager;
import io.intino.ness.ingestion.SessionHandler;
import io.intino.ness.ingestion.SessionManager;
import io.intino.ness.ingestion.SetSession;
import org.apache.hadoop.fs.Path;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;

public class SetSessionManager_ {

	private HadoopConnection connection = new HadoopConnection("hdfs://10.10.215.43:9000", "hadoop", "monentia");

	@Test
	public void should_create_and_seal_set_session() throws IOException {
		SessionHandler handler = new SessionHandler(new File("temp/localstage"));
		LocalDateTime dateTime = LocalDateTime.of(2019, 2, 28, 16, 15);
		Timetag timetag = new Timetag(dateTime, Scale.Hour);
		SetSession session = handler.createSetSession();
		for (int i = 1; i < 31; i++) session.put("tank1", timetag, "0", i);
		session.close();

		session.close();
		connection.connect();

		HadoopDatalake datalake = new HadoopDatalake(connection.fs());
		SessionManager sessionManager = new HadoopSessionManager(datalake, connection.fs(), new Path("temp/session"));
		sessionManager.push(handler.sessions());
		sessionManager.seal();
		Datalake.SetStore.Tub tub = datalake.setStore().tank("tank1").on(timetag);
		Datalake.SetStore.Set set = tub.set("0");
		ZetStream content = set.content();
		for (int i = 1; i < 31; i++) Assert.assertEquals(content.next(), i);
		MappReader index = tub.index();
		Assert.assertEquals(index.size(), 30);
		MappStream.Item next = index.next();
		Assert.assertEquals(1, next.key());
		Assert.assertEquals("0", next.value());
	}

	@After
	public void tearDown() {
		deleteDirectory(new File("temp"));
	}

	private void deleteDirectory(File directoryToBeDeleted) {
		File[] allContents = directoryToBeDeleted.listFiles();
		if (allContents != null) {
			for (File file : allContents) {
				deleteDirectory(file);
			}
		}
		directoryToBeDeleted.delete();
	}
}
