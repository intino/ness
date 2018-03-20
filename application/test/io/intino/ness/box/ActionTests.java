package io.intino.ness.box;

import io.intino.ness.box.actions.AddExternalBusAction;
import io.intino.ness.box.actions.AddJmsConnectorAction;
import io.intino.ness.box.actions.AddTankAction;
import io.intino.ness.box.actions.PauseTankAction;
import io.intino.ness.graph.ExternalBus;
import io.intino.ness.graph.NessGraph;
import io.intino.tara.magritte.Graph;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.util.FileSystemUtils;
import test.ConsumerTest;
import test.ProducerTest;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import static io.intino.ness.box.TestHelper.compileFunctions;
import static io.intino.ness.box.TestHelper.store;
import static org.junit.Assert.*;

public class ActionTests {
	private static final String TANK = "cesar.infrastructure.operation";
	private static final String TANK_FEED = "feed.cesar.infrastructure.operation";

	private static List<String> bridgedTanks = Arrays.asList("feed.cesar.infrastructure.operation", "feed.consul.server.status", "feed.consul.server.log", "feed.consul.server.upgrade", "feed.consul.feeder.status", "feed.consul.feeder.log", "feed.consul.feeder.upgrade", "feed.consul.device.status", "feed.consul.device.boot", "feed.consul.device.upgrade", "feed.consul.device.crash", "feed.consul.system.log", "feed.consul.system.statu");

	private NessGraph graph;
	private NessBox box;

	@Before
	public void setUp() throws Exception {
		String[] args = new String[]{"ness_store=../temp/store", "ness_datalake=../temp/datalake", "broker_store=../temp/broker/", "connector_id=ness-cesar-pre", "broker_port=63000", "mqtt_port=1883"};
		NessConfiguration boxConfiguration = new NessConfiguration(args);
		graph = new Graph(store(boxConfiguration.args().get("ness_store"))).loadStashes("Ness").as(NessGraph.class);
		compileFunctions(graph);
		box = (NessBox) new NessBox(boxConfiguration).put(graph.core$()).open();
	}

	@Test
	@Ignore
	public void addTank() throws Exception {
		assertEquals("shouldn't exists tanks", 0, graph.tankList().size());
		new AddTankAction(box, TANK).execute();
		assertEquals("added to the graph", 1, graph.tankList().size());
		assertTrue(new File(box.datalakeManager().getStationDirectory(), TANK).exists());
		assertTrue(box.busService().pipes().containsKey(graph.tank(0).feedQN() + "#" + graph.tank(0).flowQN()));
		checkProduceAndConsume();
		waitFinish();
	}

	@Test
	@Ignore
	public void pauseTank() throws Exception {
		new AddTankAction(box, TANK).execute();
		assertEquals("added to the graph", 1, graph.tankList().size());
		new PauseTankAction(box, TANK).execute();
		checkFailsProduceAndConsume();
		waitFinish();
	}

	@Test
	@Ignore
	public void addExternalBus() throws Exception {
		new AddExternalBusAction(box, "monentia-pro", "tcp://bus.monentia.com:61616", "cesar", "trust8&sheet").execute();
		assertEquals("added bus to the graph", 1, graph.externalBusList().size());
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
		new Thread(() -> assertTrue(new ConsumerTest().setTopic(topic).checkConsume())).start();
	}

	private void checkConsume() {
		new Thread(() -> assertTrue(new ConsumerTest().checkConsume())).start();
	}

	private void checkFailConsume() {
		new Thread(() -> assertFalse(new ConsumerTest().checkConsume())).start();
	}

	private void checkProduce() {
		new Thread(() -> new ProducerTest().produceDialogs()).start();
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
