package io.intino.ness.box;

import io.intino.ness.bus.BusManager;
import io.intino.ness.datalake.DatalakeManager;
import io.intino.ness.datalake.FileStation;
import io.intino.ness.graph.Aqueduct;
import io.intino.ness.graph.NessGraph;
import io.intino.ness.graph.Pipe;
import io.intino.tara.magritte.Graph;

public class NessBox extends AbstractBox {

	private DatalakeManager datalakeManager;
	private NessGraph graph;
	private BusManager busManager;

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
		busManager = new BusManager(this);
		busManager.start();
		datalakeManager = new DatalakeManager(new FileStation(configuration.args().get("ness_datalake")), busManager);
		initAqueducts();
		initPipes();
		return this;
	}

	private void initPipes() {
		for (Pipe pipe : graph.pipeList()) busManager().pipe(pipe.origin(), pipe.destination());
	}

	private void initAqueducts() {
		for (Aqueduct aqueduct : ness().aqueductList()) datalakeManager.startAqueduct(aqueduct);
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