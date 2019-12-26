package io.intino.datahub;

import io.intino.alexandria.datalake.file.FileDatalake;
import io.intino.alexandria.logger.Logger;
import io.intino.alexandria.sealing.FileSessionSealer;
import io.intino.alexandria.sealing.SessionSealer;
import io.intino.datahub.broker.BrokerService;
import io.intino.datahub.broker.jms.JmsBrokerService;
import io.intino.datahub.datalake.BrokerSessionSealer;
import io.intino.datahub.graph.NessGraph;

import java.io.File;

public class DataHub {
	private final NessGraph graph;
	private final File stageDirectory;
	private FileDatalake datalake;
	private BrokerService brokerService;
	private SessionSealer sessionSealer;
	private BrokerSessionSealer brokerSealer;

	public DataHub(NessGraph graph, File stageDirectory) {
		this.graph = graph;
		this.stageDirectory = stageDirectory;
		this.stageDirectory.mkdirs();
		load();
	}

	private void load() {
		if (this.graph.broker() != null && graph.broker().implementation() == null)
			graph.broker().implementation(() -> new JmsBrokerService(graph, brokerStage()));
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

	public FileDatalake datalake() {
		return datalake;
	}

	public SessionSealer sessionSealer() {
		return sessionSealer;
	}

	public SessionSealer brokerSessionSealer() {
		return brokerSealer;
	}

	public BrokerService brokerService() {
		return brokerService;
	}

	public File stage() {
		return stageDirectory;
	}

	public File brokerStage() {
		return new File(graph.broker().path(), "stage");
	}

	private void configureDatalake() {
		this.datalake = new FileDatalake(new File(graph.datalake().path()));
		this.sessionSealer = new FileSessionSealer(datalake, stageDirectory);
	}

	private void configureBroker() {
		brokerService = graph.broker().implementation().get();
		this.brokerSealer = new BrokerSessionSealer(this.datalake, brokerStage(), stageDirectory);
		try {
			brokerService.start();
		} catch (Exception e) {
			Logger.error(e);
		}
	}
}