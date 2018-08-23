package test;

import io.intino.konos.datalake.Datalake;
import io.intino.konos.datalake.Ness;
import io.intino.konos.jms.TopicConsumer;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConsumerTest {
	private static final Logger logger = LoggerFactory.getLogger(ConsumerTest.class);
	private String url = "tcp://localhost:63000";
	private String user = "test";
	private String password = "test";
	private String tankName = "consul.serverstatus";
	private Ness ness;
	private Datalake.Tank tank;


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
			ness = new Ness(url, user, password, "test-consumer");
			ness.connect();
			tank = ness.add(tankName);
			tank.handler(m -> System.out.println(m.toString()));
			final TopicConsumer flow = tank.flow("test-" + tankName);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}
}