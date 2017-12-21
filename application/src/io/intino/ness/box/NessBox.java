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
		this.connectorID = configuration.args().getOrDefault("connector_id", "ness");
		this.reflowSession = new ReflowSession(this);
	}

	@Override
	public io.intino.konos.alexandria.Box put(Object o) {
		if (o instanceof Graph) this.graph = ((Graph) o).as(NessGraph.class);
		return this;
	}

	public io.intino.konos.alexandria.Box open() {
		super.open();
		createBus(true);
		busManager.start();
		busManager.createQueue(REFLOW_READY);
		busManager.registerConsumer("service.graph.reflow", reflowSession);
		datalakeManager = new DatalakeManager(configuration.args().get("ness_datalake"));
		return this;
	}

	public void restartBus(boolean persistent) {
		busManager.stop();
		createBus(persistent);
		busManager.start();
		busManager().registerConsumer("service.graph.reflow", reflowSession);
	}

	private void createBus(boolean persistent) {
		busService = new BusService(brokerPort(), mqttPort(), persistent, new File(brokerStore()), users(), graph.tankList(), graph.jMSConnectorList());
		busManager = new BusManager(connectorID, busService);
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

	private int brokerPort() {
		return Integer.parseInt(configuration().args().get("broker_port"));
	}

	private String brokerStore() {
		return configuration().args().get("broker_store");
	}

	private int mqttPort() {
		return Integer.parseInt(configuration().args().get("mqtt_port"));
	}
}