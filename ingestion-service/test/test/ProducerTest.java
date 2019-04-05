package test;

import io.intino.alexandria.inl.Message;
import io.intino.alexandria.nessaccessor.NessAccessor;
import io.intino.alexandria.nessaccessor.tcp.TCPDatalake;
import io.intino.alexandria.nessaccessor.tcp.TCPEventStore;
import io.intino.ness.datalake.Datalake;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.time.Instant;

public class ProducerTest {
	private String url = "tcp://localhost:63000";
	private String user = "test";
	private String password = "test";
	private String tank = "consul.serverstatus";
	private NessAccessor accessor;

	@Before
	public void setUp() {
		accessor = new NessAccessor(new TCPDatalake(url, user, password, ""));
		accessor.connection().connect();
	}

	@Test
	@Ignore
	public void sendAttachment() {
		NessAccessor ness = new NessAccessor(new TCPDatalake(url, user, password, ""));
		ness.connection().connect();
		Datalake.EventStore.Tank tank = ness.eventStore().tank(this.tank);
		final Message message = new Message("status").set("name", "dialog1");
		message.set("ts", Instant.now().toString());
		message.attach("value", "txt", "example".getBytes());
		((TCPEventStore) ness.eventStore()).feed(tank.name(), message);
	}

	@Test
	@Ignore
	public void sendMessage() {
		((TCPEventStore) accessor.eventStore()).feed(tank, new Message("status").set("name", "dialog1").set("ts", Instant.now().toString()));
	}
}