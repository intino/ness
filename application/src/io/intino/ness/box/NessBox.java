package io.intino.ness.box;

import io.intino.ness.bus.BusManager;
import io.intino.ness.bus.BusPipeManager;
import io.intino.ness.datalake.DatalakeManager;
import io.intino.ness.datalake.FileStation;
import io.intino.ness.datalake.reflow.ReflowSession;
import io.intino.ness.graph.BusPipe;
import io.intino.ness.graph.ExternalBus;
import io.intino.ness.graph.NessGraph;
import io.intino.ness.graph.Pipe;
import io.intino.tara.magritte.Graph;

import java.util.ArrayList;
import java.util.List;

public class NessBox extends AbstractBox {
	private static final String REFLOW_READY = "service.ness.reflow.ready";
	private DatalakeManager datalakeManager;
	private NessGraph graph;
	private BusManager busManager;
	private ReflowSession reflowSession;
	private List<BusPipeManager> busPipeManagers = new ArrayList<>();

	public NessBox(String[] args) {
		super(args);
	}

	public NessBox(NessConfiguration configuration) {
		super(configuration);
	}

	@Override
	public io.intino.konos.Box put(Object o) {
		if (o instanceof Graph) this.graph = ((Graph) o).as(NessGraph.class);
		return this;
	}

	public io.intino.konos.Box open() {
		super.open();
		busManager = new BusManager(this, true);
		busManager.start();
		datalakeManager = new DatalakeManager(new FileStation(configuration.args().get("ness_datalake")), busManager);
		reflowSession = new ReflowSession(this);
		initBusPipeManagers();
		initPipes();
		busManager().createQueue(REFLOW_READY);
		busManager().registerConsumer("service.ness.reflow", reflowSession);
		return this;
	}

	private void initPipes() {
		for (Pipe pipe : graph.pipeList()) datalakeManager().pipe(pipe);
	}

	public void restartBusWithoutPersistence() {
		for (BusPipeManager busPipeManager : busPipeManagers) busPipeManager.stop();
		busManager.stop();
		busManager = new BusManager(this, false);
		busManager.start();
		busManager().registerConsumer("service.ness.reflow", reflowSession);
	}

	public void restartBus() {
		busManager.stop();
		busManager = new BusManager(this, true);
		datalakeManager().busManager(busManager);
		busManager.start();
		busManager().registerConsumer("service.ness.reflow", reflowSession);
		for (BusPipeManager busPipeManager : busPipeManagers) busPipeManager.start();
	}

	private void initBusPipeManagers() {
		for (ExternalBus externalBus : graph.externalBusList()) {
			BusPipeManager busPipeManager = new BusPipeManager(busManager, externalBus);
			for (BusPipe busPipe : graph.busPipeList()) if (busPipe.bus().equals(externalBus)) busPipeManager.addPipe(busPipe);
			busPipeManager.start();
			busPipeManagers.add(busPipeManager);
		}

	}

	public void close() {
		super.close();
		datalakeManager.quit();
	}

	public NessGraph ness() {
		return this.graph;
	}

	public DatalakeManager datalakeManager() {
		return datalakeManager;
	}


	public List<BusPipeManager> busPipeManagers() {
		return busPipeManagers;
	}

	public BusManager busManager() {
		return busManager;
	}

	public int brokerPort() {
		return Integer.parseInt(configuration().args().get("broker_port"));
	}

	public String brokerStore() {
		return configuration().args().get("broker_store");
	}

	public String mqttPort() {
		return configuration().args().get("mqtt_port");
	}

	public String brokerKeyStore() {
		return configuration().args().get("broker_keyStore");
	}
}