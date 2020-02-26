package io.intino.datahub;

import io.intino.alexandria.datalake.file.FileDatalake;
import io.intino.alexandria.logger.Logger;
import io.intino.alexandria.sealing.FileSessionSealer;
import io.intino.alexandria.sealing.SessionSealer;
import io.intino.datahub.broker.BrokerService;
import io.intino.datahub.broker.jms.JmsBrokerService;
import io.intino.datahub.datalake.BrokerSessions;
import io.intino.datahub.graph.NessGraph;
import io.intino.datahub.service.jms.NessService;
import io.intino.datahub.service.scheduling.Sentinels;

import java.io.File;

public class DataHub {
	private final NessGraph graph;
	private final File stageDirectory;
	private FileDatalake datalake;
	private BrokerService brokerService;
	private SessionSealer sessionSealer;
	private BrokerSessions brokerSessions;
	private NessService nessService;
	private Sentinels sentinels;

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
		if (graph.broker() != null) {
			configureBroker();
			nessService = new NessService(this);
		}
		sentinels = new Sentinels(this);
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
		sentinels.stop();
	}

	public FileDatalake datalake() {
		return datalake;
	}

	public SessionSealer sessionSealer() {
		return sessionSealer;
	}

	public BrokerSessions brokerSessions() {
		return brokerSessions;
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
		this.brokerSessions = new BrokerSessions(brokerStage(), stageDirectory);
		try {
			brokerService.start();
		} catch (Exception e) {
			Logger.error(e);
		}
	}

	public NessGraph graph() {
		return graph;
	}

	public File backupDirectory() {
		return null;
	}

	public File datalakeBackupDirectory() {
		return null;
	}

	public File sessionsBackupDirectory() {
		return null;
	}
}