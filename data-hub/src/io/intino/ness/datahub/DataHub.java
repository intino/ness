package io.intino.ness.datahub;

import io.intino.alexandria.Scale;
import io.intino.alexandria.datalake.Datalake;
import io.intino.alexandria.datalake.file.FileDatalake;
import io.intino.alexandria.logger.Logger;
import io.intino.alexandria.sealing.FileSessionSealer;
import io.intino.alexandria.sealing.SessionSealer;
import io.intino.ness.datahub.broker.jms.BrokerManager;
import io.intino.ness.datahub.broker.jms.JmsBrokerService;
import io.intino.ness.datahub.broker.jms.PipeManager;
import io.intino.ness.datahub.broker.jms.TankManager;
import io.intino.ness.datahub.graph.Broker;
import io.intino.ness.datahub.graph.NessGraph;
import org.apache.log4j.Level;

import java.io.File;

public class DataHub {
	static {
		io.intino.alexandria.logger4j.Logger.init(Level.WARN);
	}

	private final File brokerStage;
	private final NessGraph graph;
	private final File root;
	private Datalake datalake;
	private BrokerManager brokerManager;
	private JmsBrokerService jmsBrokerService;
	private SessionSealer sessionSealer;
	private PipeManager pipeManager;

	public DataHub(NessGraph graph, File root) {
		this.graph = graph;
		this.root = root;
		this.brokerStage = new File(this.root, "broker_stage");
		this.brokerStage.mkdirs();
		this.root.mkdirs();
		stageFolder().mkdirs();
	}

	public void start() {
		if (graph.datalake() != null) configureDatalake();
		if (graph.broker() != null) configureBroker();
	}

	public void stop() {
		Logger.info("Shutting down datalake...");
		if (brokerManager != null) brokerManager.stop();
	}

	public NessGraph graph() {
		return graph;
	}

	public Datalake datalake() {
		return datalake;
	}

	public SessionSealer sessionSealer() {
		return sessionSealer;
	}

	public JmsBrokerService brokerEngine() {
		return jmsBrokerService;
	}

	public File stageFolder() {
		return new File(this.root, "stage");
	}

	private void configureDatalake() {
		this.datalake = new FileDatalake(new File(graph.datalake().path()));
		this.sessionSealer = new FileSessionSealer((FileDatalake) datalake, stageFolder());
	}

	private void configureBroker() {
		jmsBrokerService = new JmsBrokerService(brokerDirectory(), graph);
		brokerManager = new BrokerManager(jmsBrokerService);
		brokerManager.start();
		pipeManager = new PipeManager(brokerManager);
		startServices();
	}


	private void startServices() {
		if (graph.broker() != null) {
			if (graph.datalake() != null) initTanks(Scale.valueOf(graph.datalake().scale().name()));
			initPipes();
		}
	}

	private void initTanks(Scale scale) {
		datalake.eventStore().tanks().forEach(t -> new TankManager(brokerManager, brokerStage, t, scale).register());
	}

	private void initPipes() {
		for (Broker.Pipe pipe : graph.broker().pipeList()) pipeManager.start(pipe);
	}

	private File brokerDirectory() {
		return new File(this.root, "broker");
	}
}