package io.intino.ness.box;

import io.intino.ness.box.actions.AddExternalBusAction;
import io.intino.ness.box.actions.AddJmsConnectorAction;
import io.intino.ness.box.actions.AddTankAction;
import io.intino.ness.box.actions.PauseTankAction;
import io.intino.ness.graph.NessGraph;
import io.intino.tara.magritte.Graph;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.util.FileSystemUtils;
import test.ConsumerTest;
import test.ProducerTest;

import java.io.File;

import static io.intino.ness.box.TestHelper.compileFunctions;
import static io.intino.ness.box.TestHelper.store;
import static java.util.Collections.singletonList;
import static org.junit.Assert.*;

public class ActionTests {
	private static final String TANK = "cesar.infrastructure.operation";

	private NessGraph graph;
	private NessBox box;

	@Before
	public void setUp() throws Exception {
		String[] args = new String[]{"ness_store=../temp/store", "ness_datalake=../temp/datalake", "connector_id", "ness-cesar-pre", "broker_store=../temp/broker/", "broker_port=63000", "mqtt_port=1883"};
		NessConfiguration boxConfiguration = new NessConfiguration(args);
		graph = new Graph(store(boxConfiguration.args().get("ness_store"))).loadStashes("Ness").as(NessGraph.class);
		compileFunctions(graph);
		box = (NessBox) new NessBox(boxConfiguration).put(graph.core$()).open();
	}

	@Test
	public void addTank() throws Exception {
		assertEquals("shouldn't exists tanks", 0, graph.tankList().size());
		new AddTankAction(box, TANK).execute();
		assertEquals("added to the graph", 1, graph.tankList().size());
		assertNotNull(box.datalakeManager().station().exists(TANK));
		box.busService().pipes().containsKey(graph.tank(0).feedQN() + "#" + graph.tank(0).flowQN());
		checkProduceAndConsume();
		waitFinish();
	}

	@Test
	public void pauseTank() throws Exception {
		new AddTankAction(box, TANK).execute();
		assertEquals("added to the graph", 1, graph.tankList().size());
		new PauseTankAction(box, TANK).execute();
		checkFailsProduceAndConsume();
		waitFinish();
	}

	@Test
	public void addExternalBus() throws Exception {
		new AddExternalBusAction(box, "pre-monentia", "tcp://bus.monentia.es/62616", "", "").execute();
		assertEquals("added bus to the graph", 1, graph.externalBusList().size());
	}

	@Test
	public void addJmsConnector() throws Exception {
		addExternalBus();
		new AddJmsConnectorAction(box, "pre-monentia", "pre-monentia", "incoming", singletonList("example")).execute();
		checkFailsProduceAndConsume();
		waitFinish();
	}

	private void waitFinish() throws InterruptedException {
		Thread.sleep(3000);
	}


	private void checkFailsProduceAndConsume() {
		checkFailConsume();
		checkProduce();
	}

	private void checkProduceAndConsume() {
		checkConsume();
		checkProduce();
	}

	private void checkConsume() {
		new Thread(() -> assertTrue(new ConsumerTest().checkConsume())).start();
	}

	private void checkFailConsume() {
		new Thread(() -> assertFalse(new ConsumerTest().checkConsume())).start();
	}

	private void checkProduce() {
		new Thread(() -> {
			try {
				new ProducerTest().produce();
			} catch (Exception e) {
			}
		}).start();
	}

	@After
	public void tearDown() throws Exception {
		graph.clear().tank(t -> true);
		FileSystemUtils.deleteRecursively(new File("../temp/datalake"));
	}
}
