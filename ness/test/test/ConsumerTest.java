package test;

import io.intino.alexandria.nessaccessor.NessAccessor;
import io.intino.alexandria.nessaccessor.tcp.TCPDatalake;
import io.intino.ness.core.Datalake;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConsumerTest {
	private static final Logger logger = LoggerFactory.getLogger(ConsumerTest.class);
	private String url = "tcp://localhost:63000";
	private String user = "test";
	private String password = "test";
	private String tankName = "consul.serverstatus";
	private NessAccessor accessor;
	private Datalake.EventStore.Tank tank;


	public static void main(String[] args) throws InterruptedException {
		new ConsumerTest();
		Thread.sleep(10000000);
	}

	public ConsumerTest() {
		setUp();
	}

	@Before
	public void setUp() {
		try {
			accessor = new NessAccessor(new TCPDatalake(url, user, password, "test-consumer"));
			accessor.connection().connect();
			tank = accessor.eventStore().tank(tankName);
			accessor.eventStore().subscribe(tank).using("test-consumer", m -> System.out.println(m.toString()));
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}
}