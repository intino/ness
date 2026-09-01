package systems.intino.eventsourcing.datahub.box;

import io.intino.alexandria.logger.Logger;
import io.intino.magritte.framework.Graph;
import org.apache.commons.io.FileUtils;
import systems.intino.eventsourcing.datahub.box.actions.SealAction;
import systems.intino.eventsourcing.datahub.box.service.jms.NessService;
import systems.intino.eventsourcing.datahub.box.service.scheduling.Sentinels;
import systems.intino.eventsourcing.datahub.broker.BrokerService;
import systems.intino.eventsourcing.datahub.broker.jms.JmsBrokerService;
import systems.intino.eventsourcing.datahub.broker.jms.SSLConfiguration;
import systems.intino.eventsourcing.datahub.datalake.BrokerSessions;
import systems.intino.eventsourcing.datahub.datalake.seal.DatahubSessionSealer;
import systems.intino.eventsourcing.datahub.datamart.DatamartFactory;
import systems.intino.eventsourcing.datahub.datamart.MasterDatamart;
import systems.intino.eventsourcing.datahub.datamart.MasterDatamartRepository;
import systems.intino.eventsourcing.datahub.datamart.impl.LocalDatamart;
import systems.intino.eventsourcing.datahub.model.Datamart;
import systems.intino.eventsourcing.datahub.model.Message;
import systems.intino.eventsourcing.datahub.model.NessGraph;
import systems.intino.eventsourcing.datalake.file.FileDatalake;
import systems.intino.eventsourcing.sealing.FileSessionSealer;
import systems.intino.eventsourcing.sealing.SessionSealer;

import java.io.File;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class DatahubBox extends AbstractBox {

	public static final String SUBJECT_EXTENSION = ".oss";
	public static final String INDICATOR_EXTENSION = ".indicator";

	private FileDatalake datalake;
	private BrokerService brokerService;
	private BrokerSessions brokerSessions;
	private NessService nessService;
	private Sentinels sentinels;
	private NessGraph graph;
	private Instant lastSeal;
	private MasterDatamartRepository masterDatamarts;

	public DatahubBox(String[] args) {
		super(args);
	}

	public DatahubBox(DatahubConfiguration configuration) {
		super(configuration);
	}

	@Override
	public io.intino.alexandria.core.Box put(Object o) {
		super.put(o);
		if (o instanceof Graph) {
			graph = ((Graph) o).as(NessGraph.class);
			injectJmsConfiguration();
			setupFromGraph();
		}
		if (o instanceof NessGraph) {
			graph = (NessGraph) o;
			injectJmsConfiguration();
			setupFromGraph();
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

	public File datamartSubjectsDirectory(String name) {
		return new File(datamartDirectory(name), "subjects");
	}

	public File datamartIndicatorsDirectory(String name) {
		return new File(datamartDirectory(name), "indicators");
	}

	public String datamartSource(String datamartName) {
		return datamarts().get(datamartName).subjectsStore().source();
	}

	public List<File> datamartIndicatorFiles(String datamartName) {
		return (List<File>) listFiles(datamartIndicatorsDirectory(datamartName), INDICATOR_EXTENSION);
	}

	private List<File> listFiles(File directory, String extension, String id) {
		if (!directory.exists() || !directory.isDirectory()) return Collections.emptyList();
		Collection<File> files = listFiles(directory, extension);
		if (id != null && !id.isEmpty()) return files.stream().filter(f -> f.getName().equals(id + extension)).toList();
		return files instanceof List<File> list ? list : new ArrayList<>(files);
	}

	private static Collection<File> listFiles(File directory, String extension) {
		if (!directory.exists() || !directory.isDirectory()) return Collections.emptyList();
		return FileUtils.listFiles(directory, new String[]{extension, extension.substring(extension.indexOf('.') + 1)}, true);
	}

	private void setupFromGraph() {
		stageDirectory().mkdirs();
		loadBrokerService();
		if (graph.datalake() != null) this.datalake = new FileDatalake(datalakeDirectory());
		sentinels = new Sentinels(this);
		if (graph.broker() != null) {
			brokerService = graph.broker().implementation().get();
			this.brokerSessions = new BrokerSessions(brokerStage(), stageDirectory());
		}
		if (graph.datamartList() != null && !graph.datamartList().isEmpty()) {
			this.masterDatamarts = new MasterDatamartRepository(datamartsDirectory());
		}
	}

	public void beforeStart() {
		seal();
		initDatamarts();
		if (graph.broker() != null) startBroker();
	}

	public void seal() {
		new SealAction(this).execute();
	}

	private File datalakeDirectory() {
		return new File(configuration.home(), "datalake");
	}

	public void afterStart() {
		for (Datamart datamart : graph.datamartList()) nessService.notifyDatamartReload(datamart.name$());
	}

	public void beforeStop() {

	}

	public void afterStop() {
		if (datamarts() != null)
			datamarts().datamarts().forEach(MasterDatamart::close);
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
			nessService = new NessService(this);
			brokerService.start();
			nessService.start();
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

	private void initDatamarts() {
		DatamartFactory datamartFactory = new DatamartFactory(this, datalake);
		long start = System.currentTimeMillis();
		for (Datamart datamart : graph.datamartList()) initDatamart(datamartFactory, datamart);
		if (!graph.datamartList().isEmpty())
			Logger.info("MasterDatamarts initialized (" + graph.datamartList().size() + ") after " + (System.currentTimeMillis() - start) + " ms");
	}

	private void initDatamart(DatamartFactory datamartFactory, Datamart datamart) {
		try {
			Logger.info("Initializing MasterDatamart " + datamart.name$() + "...");
			masterDatamarts.put(datamart.name$(), datamartFactory.create(datamart));
			Logger.debug("MasterDatamart " + datamart.name$() + " initialized!");
		} catch (Throwable e) {
			Logger.error("Could not initialize datamart " + datamart.name$() + ": " + e.getMessage(), e);
			masterDatamarts.put(datamart.name$(), new LocalDatamart(this, datamart));
		}
	}
}