import io.intino.alexandria.Scale;
import io.intino.alexandria.Timetag;
import io.intino.alexandria.inl.Message;
import io.intino.alexandria.zim.ZimReader;
import io.intino.ness.datalake.hadoop.HadoopConnection;
import io.intino.ness.datalake.hadoop.HadoopDatalake;
import io.intino.ness.datalake.hadoop.HadoopSessionManager;
import io.intino.ness.ingestion.EventSession;
import io.intino.ness.ingestion.SessionHandler;
import io.intino.ness.ingestion.SessionManager;
import org.apache.hadoop.fs.Path;
import org.junit.Test;

import java.io.File;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import static junit.framework.TestCase.assertEquals;

public class EventSessionManager_ {

	private HadoopConnection connection = new HadoopConnection("hdfs://10.10.215.43:9000", "hadoop", "monentia");

	@Test
	public void should_create_an_event_session() {
		SessionHandler handler = new SessionHandler(new File("temp/events"));
		EventSession session = handler.createEventSession();
		List<Message> messageList = new ArrayList<>();
		for (int i = 0; i < 30; i++) {
			LocalDateTime now = LocalDateTime.of(2019, 02, 28, 16, 15 + i);
			Message message = message(now.toInstant(ZoneOffset.UTC), i);
			messageList.add(message);
			session.put("tank1", new Timetag(now, Scale.Hour), message);
		}
		session.close();
		connection.connect();

		SessionManager sessionManager = new HadoopSessionManager(new HadoopDatalake(connection.fs()), connection.fs(), new Path("temp/session"));
		sessionManager.push(handler.sessions());
		sessionManager.seal();
		ZimReader reader = new ZimReader(new File("temp/datalake/events/tank1/2019022816.zim"));
		for (int i = 0; i < 30; i++) {
			Message next = reader.next();
			assertEquals(next.get("ts"), messageList.get(i).get("ts"));
			assertEquals(next.get("entries"), messageList.get(i).get("entries"));
		}
	}

	//TODO: merge with existing event files
	private Message message(Instant instant, int index) {
		return new Message("tank1").set("ts", instant.toString()).set("entries", index);
	}

}
