package io.intino.ness.box;

import io.intino.ness.bus.BusManager;
import io.intino.ness.bus.BusService;
import io.intino.ness.datalake.DatalakeManager;
import io.intino.ness.datalake.TankStarter;
import io.intino.ness.datalake.reflow.ReflowSession;
import io.intino.ness.graph.NessGraph;
import io.intino.ness.graph.Tank;
import io.intino.ness.graph.User;
import io.intino.tara.magritte.Graph;
import io.intino.tara.magritte.Layer;

import java.io.File;
import java.util.Map;
import java.util.stream.Collectors;

public class NessBox extends AbstractBox {
	private static final String REFLOW_READY = "service.ness.reflow.ready";
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
		datalakeManager = new DatalakeManager(configuration.args().get("ness_datalake"));
		startBus(true);
		startReflowService();
		startDatalakeTanks();
		return this;
	}

	public void restartBus(boolean persistent) {
		busManager.stop();
		startBus(persistent);
		startReflowService();
	}

	public void close() {
		super.close();
		datalakeManager.stop();
		busManager.stop();
	}

	private void startBus(boolean persistent) {
		busService = new BusService(brokerPort(), mqttPort(), persistent, new File(brokerStore()), users(), graph.tankList(), graph.jMSConnectorList());
		busManager = new BusManager(connectorID, busService);
		busManager.start();
	}

	private void startReflowService() {
		busManager.createQueue(REFLOW_READY);
		busManager.registerConsumer("service.ness.reflow", reflowSession);
	}

	private Map<String, String> users() {
		return graph.userList().stream().collect(Collectors.toMap(Layer::name$, User::password));
	}

	private void startDatalakeTanks() {
		final TankStarter tankStarter = new TankStarter(busManager(), datalakeManager());
		for (Tank tank : graph.tankList()) tankStarter.start(tank);
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