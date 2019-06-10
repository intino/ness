package io.intino.ness.triton.box;

import io.intino.alexandria.Scale;
import io.intino.alexandria.core.Box;
import io.intino.alexandria.logger.Logger;
import io.intino.ness.SessionManager;
import io.intino.ness.datalake.Datalake;
import io.intino.ness.datalake.file.FileDatalake;
import io.intino.ness.sealing.FileSessionManager;
import io.intino.ness.triton.box.actions.ResumeTankAction;
import io.intino.ness.triton.bus.BusManager;
import io.intino.ness.triton.bus.BusPipe;
import io.intino.ness.triton.bus.BusService;
import io.intino.ness.triton.datalake.AdminService;
import io.intino.ness.triton.datalake.reflow.ReflowService;
import io.intino.ness.triton.graph.Pipe;
import io.intino.ness.triton.graph.TritonGraph;
import io.intino.ness.triton.graph.User;

import java.io.File;
import java.util.Map;
import java.util.stream.Collectors;

import static java.io.File.separator;

public class ServiceBox extends AbstractBox {
	private static final String SERVICE_NESS_REFLOW = "service.ness.reflow";
	private static final String SERVICE_NESS_ADMIN = "service.ness.admin";
	private final File temporalSessionDirectory;
	private final File stageDirectory;
	private final Scale scale;
	private String connectorID;
	private Datalake datalake;
	private TritonGraph graph;
	private BusManager busManager;
	private BusService busService;
	private ReflowService reflowService;
	private AdminService adminService;
	private SessionManager sessionManager;
	private BusPipe busPipe;

	public ServiceBox(String[] args) {
		this(new ServiceConfiguration(args));
	}

	public ServiceBox(ServiceConfiguration configuration) {
		super(configuration);
		this.connectorID = configuration.args().getOrDefault("connector_id", "ness");
		this.reflowService = new ReflowService(this);
		this.adminService = new AdminService(this);
		this.scale = configuration.args().containsKey("scale") ? Scale.valueOf(configuration.args().get("scale")) : Scale.Day;
		this.temporalSessionDirectory = new File(workspace(), "stage_inl");
		this.temporalSessionDirectory.mkdirs();
		this.stageDirectory = new File(workspace(), "stage");
		this.stageDirectory.mkdirs();
	}

	@Override
	public Box put(Object o) {
		if (o instanceof TritonGraph) this.graph = (TritonGraph) o;
		return this;
	}

	public Box open() {
		super.open();
		initDatalake();
		startBus();
		startService();
		return this;
	}

	public void restartBus(boolean persistent) {
		busManager.restart(persistent);
		startService();
	}

	private void initDatalake() {
		this.datalake = new FileDatalake(new File(datalakeDirectory()));
		this.sessionManager = new FileSessionManager((FileDatalake) datalake, new File(workspace(), "sessions"), new File(workspace(), "stage"));
		graph.tankList().forEach(t -> datalake.eventStore().tank(t.name()));
	}

	private void startService() {
		startTanks();
		startPipes();
		startReflowService();
		startAdminService();
	}

	private void startPipes() {
		for (Pipe pipe : graph.pipeList()) busPipe.start(pipe);
	}

	private void startReflowService() {
		busManager.registerConsumer(SERVICE_NESS_REFLOW, reflowService);
	}

	private void startAdminService() {
		busManager.registerConsumer(SERVICE_NESS_ADMIN, adminService);
	}

	public void close() {
		Logger.info("Shutting down datalake...");
		super.close();
		busManager.stop();
	}

	public Datalake datalake() {
		return datalake;
	}

	public SessionManager sessionManager() {
		return sessionManager;
	}

	public TritonGraph graph() {
		return this.graph;
	}

	public BusManager busManager() {
		return busManager;
	}

	public BusService busService() {
		return busService;
	}

	public File temporalSession() {
		return temporalSessionDirectory;
	}

	public File stageFolder() {
		return temporalSessionDirectory;
	}

	String storeDirectory() {
		return workspace() + separator + "store";
	}

	private void startBus() {
		busService = new BusService(brokerPort(), mqttPort(), true, new File(brokerDirectory()), users(), graph.jMSConnectorList());
		busManager = new BusManager(connectorID, busService);
		busManager.start();
		busPipe = new BusPipe(busManager);

	}

	private Map<String, String> users() {
		return graph.userList().stream().collect(Collectors.toMap(user -> user.name() == null ? user.name$() : user.name(), User::password));
	}

	private void startTanks() {
		graph.tankList().forEach(t -> {
			new ResumeTankAction(this, t.name()).execute();
		});
	}

	private String workspace() {
		return configuration().args().get("workspace");
	}

	private String brokerDirectory() {
		return workspace() + separator + "broker";
	}

	private String datalakeDirectory() {
		return workspace() + separator + "datalake";
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