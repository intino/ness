package io.intino.ness.box;

import io.intino.ness.box.actions.ResumeTankAction;
import io.intino.ness.bus.BusManager;
import io.intino.ness.bus.BusService;
import io.intino.ness.datalake.DatalakeManager;
import io.intino.ness.datalake.PipeStarter;
import io.intino.ness.datalake.reflow.ReflowSession;
import io.intino.ness.graph.NessGraph;
import io.intino.ness.graph.Pipe;
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
		datalakeManager = new DatalakeManager(configuration.args().get("ness_datalake"), graph.tankList());
		startBus();
		startReflowService();
		startTanks();
		startBusPipes();
		return this;
	}

	public void restartBus(boolean persistent) {
		busManager.restart(persistent);
		startReflowService();
	}

	public void close() {
		super.close();
		busManager.stop();
	}

	private void startBus() {
		busService = new BusService(brokerPort(), mqttPort(), true, new File(brokerStore()), users(), graph.tankList(), graph.jMSConnectorList());
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

	private void startTanks() {
		for (Tank tank : graph.tankList()) new ResumeTankAction(this, tank.qualifiedName()).execute();
	}

	private void startBusPipes() {
		final PipeStarter tankStarter = new PipeStarter(busManager());
		for (Pipe pipe : graph.pipeList()) tankStarter.start(pipe);
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