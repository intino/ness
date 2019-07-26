package io.intino.ness.datahub;

import io.intino.alexandria.datalake.Datalake;
import io.intino.alexandria.datalake.file.FileDatalake;
import io.intino.alexandria.logger.Logger;
import io.intino.alexandria.sealing.FileSessionSealer;
import io.intino.alexandria.sealing.SessionSealer;
import io.intino.ness.datahub.broker.BrokerService;
import io.intino.ness.datahub.graph.NessGraph;
import org.apache.log4j.Level;

import java.io.File;

public class DataHub {
	static {
		io.intino.alexandria.logger4j.Logger.init(Level.WARN);
	}

	private final NessGraph graph;
	private final File root;
	private Datalake datalake;
	private BrokerService brokerService;
	private SessionSealer sessionSealer;

	public DataHub(NessGraph graph, File root) {
		this.graph = graph;
		this.root = root;
		File brokerStage = new File(this.root, "broker_stage");
		brokerStage.mkdirs();
		this.root.mkdirs();
		stageFolder().mkdirs();
	}

	public void start() {
		if (graph.datalake() != null) configureDatalake();
		if (graph.broker() != null) configureBroker();
	}

	public void stop() {
		Logger.info("Shutting down datalake...");
		if (brokerService != null) {
			try {
				brokerService.stop();
			} catch (Exception e) {
				Logger.error(e);
			}
		}
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

	public BrokerService brokerService() {
		return brokerService;
	}

	public File stageFolder() {
		return new File(this.root, "stage");
	}

	private void configureDatalake() {
		this.datalake = new FileDatalake(new File(graph.datalake().path()));
		this.sessionSealer = new FileSessionSealer((FileDatalake) datalake, stageFolder());
	}

	private void configureBroker() {
		brokerService = graph.broker().implementation();
		try {
			brokerService.start();
		} catch (Exception e) {
			Logger.error(e);
		}
	}
}