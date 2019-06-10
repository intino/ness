package io.intino.ness.triton.box;

import io.intino.ness.triton.box.actions.AddExternalBusAction;
import io.intino.ness.triton.box.actions.AddJmsConnectorAction;
import io.intino.ness.triton.box.actions.AddTankAction;
import io.intino.ness.triton.box.actions.PauseTankAction;
import io.intino.ness.triton.graph.ExternalBus;
import io.intino.ness.triton.graph.TritonGraph;
import io.intino.tara.magritte.Graph;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.util.FileSystemUtils;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import static io.intino.ness.triton.box.TestHelper.store;
import static org.junit.Assert.assertEquals;

public class ActionTests {
	private static final String TANK = "cesar.infrastructure.operation";
	private static final String TANK_FEED = "feed.cesar.infrastructure.operation";

	private static List<String> bridgedTanks = Arrays.asList("feed.cesar.infrastructure.operation", "feed.consul.server.status", "feed.consul.server.log", "feed.consul.server.upgrade", "feed.consul.feeder.status", "feed.consul.feeder.log", "feed.consul.feeder.upgrade", "feed.consul.device.status", "feed.consul.device.boot", "feed.consul.device.upgrade", "feed.consul.device.crash", "feed.consul.system.log", "feed.consul.system.statu");

	private ServiceBox box;
	private TritonGraph tritonGraph;

	@Before
	public void setUp() {
		String[] args = new String[]{"workspace=../temp/workspace", "ness_datalake=../temp/datalake", "broker_store=../temp/broker/", "connector_id=ness-cesar-pre", "broker_port=63000", "mqtt_port=1883"};
		ServiceConfiguration boxConfiguration = new ServiceConfiguration(args);
		tritonGraph = new Graph(store(boxConfiguration.args().get("workspace"))).loadStashes("Ness").as(TritonGraph.class);
		box = (ServiceBox) new ServiceBox(boxConfiguration).put(tritonGraph).open();
		tritonGraph = box.graph();
	}

	@Test
	@Ignore
	public void addTank() throws Exception {
		assertEquals("shouldn't exists tanks", 0, tritonGraph.tankList().size());
		new AddTankAction(box, TANK).execute();
		assertEquals("added to the graph", 1, tritonGraph.tankList().size());
//		assertTrue(new File(box.datalakeDirectory(), TANK).exists());
		checkProduceAndConsume();
		waitFinish();
	}

	@Test
	@Ignore
	public void pauseTank() throws Exception {
		new AddTankAction(box, TANK).execute();
		assertEquals("added to the graph", 1, tritonGraph.tankList().size());
		new PauseTankAction(box, TANK).execute();
		checkFailsProduceAndConsume();
		waitFinish();
	}

	@Test
	@Ignore
	public void addExternalBus() {
		new AddExternalBusAction(box, "monentia-pro", "tcp://bus.monentia.com:61616", "cesar", "trust8&sheet").execute();
		assertEquals("added bus to the graph", 1, tritonGraph.externalBusList().size());
	}

	@Test
	@Ignore
	public void addJmsConnector() throws Exception {
		addExternalBus();
		new AddJmsConnectorAction(box, "monentia-pro-connection", "monentia-pro", "incoming", String.join(" ", bridgedTanks)).execute();
		waitFinish();
	}

	@Test
	@Ignore
	public void checkJmsConnector() throws Exception {
		checkBridge(box.graph().externalBus(0), TANK_FEED);
		waitFinish();
	}

	private void checkFailsProduceAndConsume() {
		checkFailConsume();
		checkProduce();
	}

	private void checkProduceAndConsume() {
		checkConsume();
		checkProduce();
	}

	private void checkBridge(ExternalBus bus, String topic) {
		checkConsume(topic);
//		new Thread(() -> new ProducerTest(bus.originURL(), bus.user(), bus.password(), topic).produce()).start();
	}

	private void checkConsume(String topic) {
	}

	private void checkConsume() {
	}

	private void checkFailConsume() {
	}

	private void checkProduce() {
	}

	@After
	public void tearDown() throws Exception {
//		graph.clear().tank(t -> true);
//		graph.clear().externalBus(t -> true);
//		graph.clear().jMSConnector(t -> true);
		FileSystemUtils.deleteRecursively(new File("../temp/datalake"));
	}

	private void waitFinish() throws InterruptedException {
		Thread.sleep(5000);
	}
}
