package io.intino.ness.box;

import io.intino.ness.box.actions.AddTankAction;
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
import static org.junit.Assert.*;

public class ActionTests {
	private static final String TANK = "cesar.infrastructure.operation";

	private NessGraph graph;
	private NessBox box;

	@Before
	public void setUp() throws Exception {
		String[] args = new String[]{"ness_store=../temp/store", "ness_datalake=../temp/datalake", "connector_id", "ness-cesar-pre", "broker_store=./temp/broker/", "broker_port=63000", "mqtt_port=1883"};
		NessConfiguration boxConfiguration = new NessConfiguration(args);
		graph = new Graph(store(boxConfiguration.args().get("ness_store"))).loadStashes("Ness").as(NessGraph.class);
		compileFunctions(graph);
		box = (NessBox) new NessBox(boxConfiguration).put(graph.core$()).open();
	}

	@Test
	public void addTankAction() throws Exception {
		assertEquals(0, graph.tankList().size());
		final AddTankAction action = new AddTankAction();
		action.box = box;
		action.name = TANK;
		action.execute();
		Thread.sleep(2000);
		assertEquals("added to the graph",1, graph.tankList().size());
		assertNotNull(box.datalakeManager().station().tank(TANK));
		box.busService().pipes().containsKey(graph.tank(0).feedQN() + "#" + graph.tank(0).flowQN());
		new Thread(() -> {try {new ProducerTest().produce();} catch (Exception e) {}
		}).start();
		new Thread(() -> assertTrue(new ConsumerTest().checkConsume())).start();
		Thread.sleep(6000);
	}

	@After
	public void tearDown() throws Exception {
		graph.clear().tank(t -> true);
		FileSystemUtils.deleteRecursively(new File("../temp/datalake"));
	}
}
