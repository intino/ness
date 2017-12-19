package io.intino.ness.box;

import io.intino.ness.bus.BusManager;
import io.intino.ness.bus.BusService;
import io.intino.ness.datalake.DatalakeManager;
import io.intino.ness.datalake.reflow.ReflowSession;
import io.intino.ness.graph.NessGraph;
import io.intino.ness.graph.User;
import io.intino.tara.magritte.Graph;
import io.intino.tara.magritte.Layer;

import java.io.File;
import java.util.Map;
import java.util.stream.Collectors;

public class NessBox extends AbstractBox {
	private static final String REFLOW_READY = "service.graph.reflow.ready";
	private String connectorID;
	private DatalakeManager datalakeManager;
	private NessGraph graph;
	private BusManager busManager;
	private BusService busService;
	private ReflowSession reflowSession;

	public NessBox(String[] args) {
		super(args);
	}

	public NessBox(NessConfiguration configuration) {
		super(configuration);
		this.connectorID = configuration.args().containsKey("connector_id") ? configuration.args().get("connector_id") : "ness";
	}

	@Override
	public io.intino.konos.Box put(Object o) {
		if (o instanceof Graph) this.graph = ((Graph) o).as(NessGraph.class);
		return this;

	}

	public io.intino.konos.Box open() {
		super.open();
		busService = new BusService(brokerPort(), mqttPort(), true, new File(brokerStore()), users());
		busManager = new BusManager(connectorID, busService);
		busManager.start();
		datalakeManager = new DatalakeManager(graph, configuration.args().get("ness_datalake"), busManager, busService);
		reflowSession = new ReflowSession(this);
		initBusPipeManagers();
		busManager().createQueue(REFLOW_READY);
		busManager().registerConsumer("service.graph.reflow", reflowSession);
		return this;
	}

	public void restartBusWithoutPersistence() {
		busManager.stop();
		//TODO
		busManager.start();
		busManager().registerConsumer("service.graph.reflow", reflowSession);
	}

	public void restartBus() {
		busManager.stop();
		busManager = new BusManager(connectorID, busService);
		datalakeManager().busManager(busManager);
		busManager.start();
		busManager().registerConsumer("service.graph.reflow", reflowSession);
	}

	private void initBusPipeManagers() {

	}

	private Map<String, String> users() {
		return graph.userList().stream().collect(Collectors.toMap(Layer::name$, User::password));
	}

	public void close() {
		super.close();
		datalakeManager.stop();
		busManager.stop();
	}

	public NessGraph graph() {
		return this.graph;
	}

	public DatalakeManager datalakeManager() {
		return datalakeManager;
	}

	public BusManager busManager() {
		return busManager;
	}

	public BusService busService() {
		return busService;
	}

	public int brokerPort() {
		return Integer.parseInt(configuration().args().get("broker_port"));
	}

	public String brokerStore() {
		return configuration().args().get("broker_store");
	}

	public int mqttPort() {
		return Integer.parseInt(configuration().args().get("mqtt_port"));
	}
}