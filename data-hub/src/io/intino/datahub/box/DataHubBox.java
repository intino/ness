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
import io.intino.datahub.datalake.seal.DatahubSessionSealer;
import io.intino.datahub.model.NessGraph;
import io.intino.magritte.framework.Graph;
import io.intino.ness.master.core.Master;

import java.io.File;
import java.net.URL;
import java.time.Instant;

public class DataHubBox extends AbstractBox {

	private FileDatalake datalake;
	private BrokerService brokerService;
	private BrokerSessions brokerSessions;
	private NessService nessService;
	private Sentinels sentinels;
	private NessGraph graph;
	private Instant lastSeal;
	private Master master;

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
		return new DatahubSessionSealer(datalake,graph.datalake(), stageDirectory(), treatedDirectory());
	}

	private void injectJmsConfiguration() {
		if (graph.datalake() != null) {
			graph.datalake().path(datalakeDirectory().getAbsolutePath());
			if (graph.datalake().backup() != null)
				graph.datalake().backup().path(configuration.backupDirectory().getAbsolutePath());
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

	public File treatedDirectory() {
		return new File(configuration.home(), "datahub/treated");
	}

	public File mappersDirectory() {
		File mappers = new File(configuration.home(), "datahub/mappers");
		mappers.mkdirs();
		return mappers;
	}

	public SessionSealer sessionSealer(File stageDirectory) {
		return new FileSessionSealer(datalake, stageDirectory, treatedDirectory());
	}

	public Master master() {
		return master;
	}

	public void beforeStart() {
		stageDirectory().mkdirs();
		loadBrokerService();
		if (graph.datalake() != null) {
			this.datalake = new FileDatalake(datalakeDirectory());
//			initMaster();
		}
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

	private void loadBrokerService() {
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

	public void lastSeal(Instant now) {
		this.lastSeal = now;
	}

	public Instant lastSeal() {
		return lastSeal;
	}


//	private void initMaster() {
//		master = new Master(getMasterConfig());
//		master.start();
//	}
//
//	private Master.Config getMasterConfig() {
//		Master.Config config = new Master.Config();
////		config.datalakeRootPath(datalakeDirectory());
////		config.serializer(MasterSerializers.getOrDefault(configuration.masterSerializer()));
////		config.tripletsDigester(new DatahubTripletDigesterFactory().create());
//		return config;
//	}

}