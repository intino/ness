package io.intino.ness.box;

import io.intino.ness.box.actions.ResumeTankAction;
import io.intino.ness.bus.BusManager;
import io.intino.ness.bus.BusService;
import io.intino.ness.datalake.PipeStarter;
import io.intino.ness.datalake.Scale;
import io.intino.ness.datalake.graph.AbstractTank;
import io.intino.ness.datalake.graph.DatalakeGraph;
import io.intino.ness.datalake.graph.Tank;
import io.intino.ness.datalake.reflow.ReflowSession;
import io.intino.ness.graph.NessGraph;
import io.intino.ness.graph.Pipe;
import io.intino.ness.graph.User;
import io.intino.tara.magritte.Graph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Map;
import java.util.stream.Collectors;

import static java.io.File.separator;
import static org.slf4j.Logger.ROOT_LOGGER_NAME;

public class NessBox extends AbstractBox {
	private static final String SERVICE_NESS_REFLOW = "service.ness.reflow";
	private static Logger logger = LoggerFactory.getLogger(ROOT_LOGGER_NAME);
	private Scale scale = Scale.Day;
	private String connectorID;
	private DatalakeGraph datalake;
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
		this.scale = configuration.args().containsKey("scale") ? Scale.valueOf(configuration.args().get("scale")) : Scale.Day;
	}

	@Override
	public io.intino.konos.alexandria.Box put(Object o) {
		if (o instanceof Graph) {
			this.graph = ((Graph) o).as(NessGraph.class);
			this.datalake = ((Graph) o).as(DatalakeGraph.class).directory(new File(datalakeDirectory())).scale(scale);
		}
		return this;
	}

	public io.intino.konos.alexandria.Box open() {
		super.open();
		startBus();
		startService();
		return this;
	}

	public void restartBus(boolean persistent) {
		busManager.restart(persistent);
		startService();
	}

	private void startService() {
		startReflowService();
		startTanks();
		startBusPipes();
	}

	private void startReflowService() {
		busManager.registerConsumer(SERVICE_NESS_REFLOW, reflowSession);
	}

	public void close() {
		datalake().tankList().forEach(Tank::terminate);
		logger.info("tanks terminated");
		super.close();
		busManager.stop();
	}

	private void startBus() {
		busService = new BusService(brokerPort(), mqttPort(), true, new File(brokerDirectory()), users(), datalake.tankList(), graph.jMSConnectorList());
		busManager = new BusManager(connectorID, busService);
		busManager.start();
	}

	private Map<String, String> users() {
		return graph.userList().stream().collect(Collectors.toMap(user -> user.name() == null ? user.name$() : user.name(), User::password));
	}

	private void startTanks() {
		for (Tank tank : datalake.tankList().stream().filter(AbstractTank::active).collect(Collectors.toList()))
			new ResumeTankAction(this, tank.qualifiedName()).execute();
	}

	private void startBusPipes() {
		final PipeStarter tankStarter = new PipeStarter(busManager());
		for (Pipe pipe : graph.pipeList()) tankStarter.start(pipe);
	}

	public NessGraph nessGraph() {
		return this.graph;
	}

	public BusManager busManager() {
		return busManager;
	}

	public BusService busService() {
		return busService;
	}

	public String workspace() {
		return configuration().args().get("workspace");
	}

	private String datalakeDirectory() {
		return workspace() + separator + "datalake";
	}

	private String brokerDirectory() {
		return workspace() + separator + "broker";
	}

	public String storeDirectory() {
		return workspace() + separator + "store";
	}

	private int brokerPort() {
		return Integer.parseInt(configuration().args().get("broker_port"));
	}

	private int mqttPort() {
		return Integer.parseInt(configuration().args().get("mqtt_port"));
	}

	public DatalakeGraph datalake() {
		return datalake;
	}
}