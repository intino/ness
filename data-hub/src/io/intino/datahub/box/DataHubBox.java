package io.intino.datahub.box;

import io.intino.alexandria.datalake.file.FileDatalake;
import io.intino.alexandria.logger.Logger;
import io.intino.alexandria.sealing.FileSessionSealer;
import io.intino.alexandria.sealing.SessionSealer;
import io.intino.alexandria.ui.services.AuthService;
import io.intino.datahub.box.actions.SealAction;
import io.intino.datahub.box.service.jms.NessService;
import io.intino.datahub.box.service.scheduling.Sentinels;
import io.intino.datahub.broker.BrokerService;
import io.intino.datahub.broker.jms.JmsBrokerService;
import io.intino.datahub.broker.jms.SSLConfiguration;
import io.intino.datahub.datalake.BrokerSessions;
import io.intino.datahub.datalake.seal.DatahubSessionSealer;
import io.intino.datahub.datamart.DatamartFactory;
import io.intino.datahub.datamart.MasterDatamart;
import io.intino.datahub.datamart.MasterDatamartRepository;
import io.intino.datahub.datamart.impl.LocalMasterDatamart;
import io.intino.datahub.datamart.serialization.MasterDatamartSerializer;
import io.intino.datahub.model.Datamart;
import io.intino.datahub.model.Message;
import io.intino.datahub.model.NessGraph;
import io.intino.magritte.framework.Graph;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.net.URL;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class DataHubBox extends AbstractBox {

	public static final String TIMELINE_EXTENSION = ".timeline";
	public static final String REEL_EXTENSION = ".reel";

	private FileDatalake datalake;
	private BrokerService brokerService;
	private BrokerSessions brokerSessions;
	private NessService nessService;
	private Sentinels sentinels;
	private NessGraph graph;
	private Instant lastSeal;
	private MasterDatamartRepository masterDatamarts;
	private MasterDatamartSerializer datamartSerializer;

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
		return new DatahubSessionSealer(datalake, graph.datalake(), stageDirectory(), treatedDirectory());
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
		if (graph.datalake().tank(t -> t.name$().equals("Session")) == null) {
			Message session = graph.create("misc", "Session").message();
			graph.datalake().create("Session").tank().asMessage(session);
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

	public File datamartsDirectory() {
		return new File(configuration.home(), "datahub/datamarts");
	}

	public File datamartDirectory(String name) {
		return new File(datamartsDirectory(), name);
	}

	public File mappersDirectory() {
		File mappers = new File(configuration.home(), "datahub/mappers");
		mappers.mkdirs();
		return mappers;
	}

	public SessionSealer sessionSealer(File stageDirectory) {
		return new FileSessionSealer(datalake, stageDirectory, treatedDirectory());
	}

	public MasterDatamartRepository datamarts() {
		return masterDatamarts;
	}

	public File datamartTimelinesDirectory(String name) {
		return new File(datamartDirectory(name), "timelines");
	}

	public File datamartReelsDirectory(String name) {
		return new File(datamartDirectory(name), "reels");
	}

	public File datamartReelsDirectory(String name, String type) {
		File dir = datamartReelsDirectory(name);
		return type == null ? dir : new File(dir, type);
	}

	public List<File> datamartTimelineFiles(String datamartName, String id) {
		return listFiles(datamartTimelinesDirectory(datamartName), TIMELINE_EXTENSION, id);
	}

	public List<File> datamartReelFiles(String datamartName, String id, String type) {
		return listFiles(datamartReelsDirectory(datamartName, type), REEL_EXTENSION, id);
	}

	private List<File> listFiles(File directory, String extension, String id) {
		if (!directory.exists()) return Collections.emptyList();
		Collection<File> files = FileUtils.listFiles(directory, new String[]{extension, extension.substring(extension.indexOf('.') + 1)}, true);
		if (id != null && !id.isEmpty()) return files.stream().filter(f -> f.getName().equals(id + extension)).toList();
		return files instanceof List<File> list ? list : new ArrayList<>(files);
	}

	public MasterDatamartSerializer datamartSerializer() {
		return datamartSerializer;
	}

	public void beforeStart() {
		stageDirectory().mkdirs();
		loadBrokerService();
		if (graph.datalake() != null) this.datalake = new FileDatalake(datalakeDirectory());
		sentinels = new Sentinels(this);
		if (graph.broker() != null) {
			brokerService = graph.broker().implementation().get();
			this.brokerSessions = new BrokerSessions(brokerStage(), stageDirectory());
		}
		new SealAction(this).execute();
		if (graph.datamartList() != null && !graph.datamartList().isEmpty()) initMasterDatamarts();
		if (graph.broker() != null) startBroker();
	}

	private File datalakeDirectory() {
		return new File(configuration.home(), "datalake");
	}

	public void afterStart() {

	}

	public void beforeStop() {

	}

	public void afterStop() {
		datamarts().datamarts().forEach(MasterDatamart::close);
	}

	@Override
	protected AuthService authService(URL authServiceUrl) {
		return null;
	}

	private void loadBrokerService() {
		if (this.graph.broker() != null && graph.broker().implementation() == null)
			graph.broker().implementation(() -> new JmsBrokerService(this, brokerStage(), configuration.keystorePath() != null ? sslConfiguration() : null));
	}

	private SSLConfiguration sslConfiguration() {
		return new SSLConfiguration(new File(configuration.keystorePath()), new File(configuration.truststorePath()), configuration.keystorePassword().toCharArray(), configuration.truststorePassword().toCharArray());
	}

	private void startBroker() {
		try {
			brokerService.start();
			nessService = new NessService(this);
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

	public NessService nessService() {
		return nessService;
	}

	private void initMasterDatamarts() {
		this.datamartSerializer = new MasterDatamartSerializer(this);
		this.masterDatamarts = new MasterDatamartRepository(datamartsDirectory());
		initDatamarts();
	}

	private void initDatamarts() {
		DatamartFactory datamartFactory = new DatamartFactory(this, datalake);
		long start = System.currentTimeMillis();
		for (Datamart datamart : graph.datamartList()) initDatamart(datamartFactory, datamart);
		Logger.info("MasterDatamarts initialized (" + masterDatamarts.size() + ") after " + (System.currentTimeMillis() - start) + " ms");
	}

	private void initDatamart(DatamartFactory datamartFactory, Datamart datamart) {
		try {
			Logger.info("Initializing MasterDatamart " + datamart.name$() + "...");
			masterDatamarts.put(datamart.name$(), datamartFactory.create(datamart));
			Logger.debug("MasterDatamart " + datamart.name$() + " initialized!");
		} catch (Throwable e) {
			Logger.error("Could not initialize datamart " + datamart.name$() + ": " + e.getMessage(), e);
			masterDatamarts.put(datamart.name$(), new LocalMasterDatamart(this, datamart));
		}
	}
}