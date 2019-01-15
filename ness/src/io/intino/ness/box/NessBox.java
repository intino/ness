package io.intino.ness.box;

import io.intino.alexandria.Scale;
import io.intino.alexandria.core.Box;
import io.intino.alexandria.logger.Logger;
import io.intino.ness.box.actions.ResumeTankAction;
import io.intino.ness.bus.BusManager;
import io.intino.ness.bus.BusService;
import io.intino.ness.core.fs.FSDatalake;
import io.intino.ness.datalake.AdminService;
import io.intino.ness.datalake.PipeStarter;
import io.intino.ness.datalake.reflow.ReflowService;
import io.intino.ness.graph.NessGraph;
import io.intino.ness.graph.Pipe;
import io.intino.ness.graph.User;

import java.io.File;
import java.util.Map;
import java.util.stream.Collectors;

import static java.io.File.separator;

public class NessBox extends AbstractBox {
	private static final String SERVICE_NESS_REFLOW = "service.ness.reflow";
	private static final String SERVICE_NESS_ADMIN = "service.ness.admin";
	private Scale scale = Scale.Day;
	private String connectorID;
	private FSDatalake datalake;
	private NessGraph graph;
	private BusManager busManager;
	private BusService busService;
	private ReflowService reflowService;
	private AdminService adminService;

	public NessBox(String[] args) {
		super(args);
	}

	public NessBox(NessConfiguration configuration) {
		super(configuration);
		this.connectorID = configuration.args().getOrDefault("connector_id", "ness");
		this.reflowService = new ReflowService(this);
		this.adminService = new AdminService(this);
		this.scale = configuration.args().containsKey("scale") ? Scale.valueOf(configuration.args().get("scale")) : Scale.Day;
		this.datalake = new FSDatalake(new File(datalakeDirectory()));
	}

	@Override
	public Box put(Object o) {
		if (o instanceof NessGraph) this.graph = (NessGraph) o;
		return this;
	}

	public Box open() {
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
		startTanks();
		startBusPipes();
		startReflowService();
		startAdminService();
	}

	private void startReflowService() {
		busManager.registerConsumer(SERVICE_NESS_REFLOW, reflowService);
	}

	private void startAdminService() {
		busManager.registerConsumer(SERVICE_NESS_ADMIN, adminService);
	}

	public void close() {
		Logger.info("Shutting down datalake...");
//		this.datalake.eventStore().tanks().forEach(Tank::terminate);
		super.close();
		busManager.stop();
	}

	private void startBus() {
		busService = new BusService(brokerPort(), mqttPort(), true, new File(brokerDirectory()), users(), graph.jMSConnectorList());
		busManager = new BusManager(connectorID, busService);
		busManager.start();
	}

	private Map<String, String> users() {
		return graph.userList().stream().collect(Collectors.toMap(user -> user.name() == null ? user.name$() : user.name(), User::password));
	}

	private void startTanks() {
		graph.tankList().forEach(t -> {
			new ResumeTankAction(this, t.name()).execute();
		});
	}

	private void startBusPipes() {
		final PipeStarter tankStarter = new PipeStarter(busManager());
		for (Pipe pipe : graph.pipeList()) tankStarter.start(pipe);
	}

	public FSDatalake datalake() {
		return datalake;
	}

	public NessGraph graph() {
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

	public String storeDirectory() {
		return workspace() + separator + "store";
	}

	public String datalakeDirectory() {
		return workspace() + separator + "datalake";
	}

	public String datalakeStageDirectory() {
		return datalakeDirectory() + separator + "stage";
	}

	private String brokerDirectory() {
		return workspace() + separator + "broker";
	}

	private int brokerPort() {
		return Integer.parseInt(configuration().args().get("broker_port"));
	}

	private int mqttPort() {
		return Integer.parseInt(configuration().args().get("mqtt_port"));
	}

	public Scale scale() {
		return scale;
	}
}