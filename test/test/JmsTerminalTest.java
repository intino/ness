import org.junit.After;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import systems.intino.eventsourcing.datahubterminal.Broker;
import systems.intino.eventsourcing.datahubterminal.JmsConnector;
import systems.intino.eventsourcing.datahubterminal.remotedatalake.RemoteDatalake;
import systems.intino.eventsourcing.datalake.Datalake;
import systems.intino.eventsourcing.event.message.MessageEvent;
import systems.intino.eventsourcing.jms.ConnectionConfig;

import java.io.File;
import java.time.Instant;
import java.util.stream.Stream;

public class JmsTerminalTest {

	@Test
	public void testRemoteDatalake() {
		JmsConnector connector = new JmsConnector(new ConnectionConfig("tcp://localhost:63000?jms.blobTransferPolicy.uploadUrl=http://localhost:8081", "trooper", "trooper", "test"), null);
		connector.start();
		Datalake.Store.Tank<MessageEvent> tank = new RemoteDatalake(connector).messageStore().tank("operation.Inventario");
		if (tank == null) Assert.fail();
		Stream<MessageEvent> content = tank.content();
		content.forEach(e -> System.out.println(e.ts()));
	}

	@Test
	@Ignore
	public void testMessageOutBox() throws InterruptedException {
		JmsConnector connector = new JmsConnector(new ConnectionConfig("tcp://localhost:63000", "user1", "1234", ""), new File("outBox"));
		while (true) {
			connector.sendEvent("lalala", new TestEvent("tt").field1("v1"));
			Thread.sleep(10000);
		}
	}

	@Test
	@Ignore
	public void testPutAndHandle() throws InterruptedException {
		JmsConnector connector = new JmsConnector(new ConnectionConfig("failover:(tcp://localhost:63000)", "comercial.cuentamaestra", "comercial.cuentamaestra", "cobranza"), new File("outBox"));
//		new Thread(() -> connector.attachListener("lalala", m -> System.out.println(m.toString()))).start();
		while (true) {
			connector.sendEvent("comercial.cuentamaestra.GestionCobro", new TestEvent("GestionCobro").field1("v1").ts(Instant.now()));
			Thread.sleep(10000);
		}
	}

	@After
	public void tearDown() throws Exception {
//		for (File outBox : Objects.requireNonNull(new File("outBox").listFiles())) Files.delete(outBox.toPath());
	}

	public static class TestEvent extends MessageEvent {

		public TestEvent(String type) {
			super(type, "test");
		}

		public String field1() {
			return toMessage().get("field1").asString();
		}

		public TestEvent field1(String value) {
			super.toMessage().set("field1", value);
			return this;
		}

		@Override
		public Instant ts() {
			return Instant.now();
		}

		@Override
		public String ss() {
			return "test";
		}

		@Override
		public Format format() {
			return Format.Message;
		}
	}

	@Test
	public void name() {
		Broker.isRunning("failover:(tcp://localhost:62000)?waitUntilConnect=true");
	}
}
