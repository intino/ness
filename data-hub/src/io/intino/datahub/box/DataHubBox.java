package io.intino.datahub.box;

import io.intino.alexandria.datalake.file.FileDatalake;
import io.intino.alexandria.logger.Logger;
import io.intino.alexandria.sealing.FileSessionSealer;
import io.intino.alexandria.sealing.SessionSealer;
import io.intino.alexandria.ui.services.AuthService;
import io.intino.datahub.box.service.jms.NessService;
import io.intino.datahub.box.service.scheduling.Sentinels;
import io.intino.datahub.broker.BrokerService;
import io.intino.datahub.broker.jms.JmsBrokerService;
import io.intino.datahub.datalake.BrokerSessions;
import io.intino.datahub.graph.NessGraph;
import io.intino.magritte.framework.Graph;

import java.io.File;
import java.net.URL;

public class DataHubBox extends AbstractBox {
	private FileDatalake datalake;
	private BrokerService brokerService;
	private BrokerSessions brokerSessions;
	private NessService nessService;
	private Sentinels sentinels;
	private NessGraph graph;

	public DataHubBox(String[] args) {
		super(args);
	}

	public DataHubBox(DataHubConfiguration configuration) {
		super(configuration);
	}

	@Override
	public io.intino.alexandria.core.Box put(Object o) {
		super.put(o);
		if (o instanceof Graph) {
			graph = ((Graph) o).as(NessGraph.class);
			injectJmsConfiguration();
		}
		if (o instanceof NessGraph) {
			graph = (NessGraph) o;
			injectJmsConfiguration();
		}
		return this;
	}

	public BrokerService brokerService() {
		return brokerService;
	}

	public NessGraph graph() {
		return graph;
	}

	public BrokerSessions brokerSessions() {
		return brokerSessions;
	}

	public SessionSealer sessionSealer() {
		return new FileSessionSealer(datalake, stageDirectory());
	}

	private void injectJmsConfiguration() {
		if (graph.datalake() != null) {
			graph.datalake().path(datalakeDirectory().getAbsolutePath());
			if (graph.datalake().backup() != null) graph.datalake().backup().path(configuration.backupDirectory());
		}
		if (graph.broker() != null) {
			graph.broker().path(brokerDirectory().getAbsolutePath());
			graph.broker().port(Integer.parseInt(configuration.brokerPort()));
			graph.broker().secondaryPort(Integer.parseInt(configuration.brokerSecondaryPort()));
		}
	}

	private File brokerDirectory() {
		return new File(configuration.home(), "datahub/broker");
	}

	public File stageDirectory() {
		return new File(configuration.home(), "datahub/stage");
	}

	public File mappersDirectory() {
		File mappers = new File(configuration.home(), "datahub/mappers");
		mappers.mkdirs();
		return mappers;
	}

	public SessionSealer sessionSealer(File stageDirectory) {
		return new FileSessionSealer(datalake, stageDirectory);
	}

	public void beforeStart() {
		stageDirectory().mkdirs();
		load();
		if (graph.datalake() != null) this.datalake = new FileDatalake(datalakeDirectory());
		if (graph.broker() != null) {
			configureBroker();
			nessService = new NessService(this);
		}
		sentinels = new Sentinels(this);
	}

	private File datalakeDirectory() {
		return new File(configuration.home(), "datalake");
	}

	public void afterStart() {

	}

	public void beforeStop() {

	}

	public void afterStop() {

	}

	@Override
	protected AuthService authService(URL authServiceUrl) {
		return null;
	}

	private void load() {
		if (this.graph.broker() != null && graph.broker().implementation() == null)
			graph.broker().implementation(() -> new JmsBrokerService(graph, brokerStage()));
	}

	private void configureBroker() {
		brokerService = graph.broker().implementation().get();
		this.brokerSessions = new BrokerSessions(brokerStage(), stageDirectory());
		try {
			brokerService.start();
		} catch (Exception e) {
			Logger.error(e);
		}
	}

	private File brokerStage() {
		return new File(brokerDirectory(), "stage");
	}

	public FileDatalake datalake() {
		return datalake;
	}
}