package io.intino.ness.box;

import io.intino.ness.DatalakeManager;
import io.intino.ness.bus.BusManager;
import io.intino.ness.datalake.FileStation;
import io.intino.ness.graph.NessGraph;
import io.intino.tara.magritte.Graph;

import java.util.Collections;

public class NessBox extends AbstractBox {

	private DatalakeManager datalakeManager;
	private Graph graph;
	private BusManager busManager;

	public NessBox(String[] args) {
		super(args);
	}

	@Override
	public io.intino.konos.Box put(Object o) {
		if (o instanceof Graph) this.graph = (Graph) o;
		return this;
	}

	public io.intino.konos.Box open() {
		super.open();
		busManager = new BusManager(this);
		busManager.start();
		datalakeManager = new DatalakeManager(new FileStation(configuration.args().get("ness.rootPath")), busManager);
		System.out.println(busManager.addUser("cesar", Collections.emptyList()));
		return this;
	}

	public void close() {
		super.close();
		datalakeManager.quit();

	}

	public NessGraph ness() {
		return this.graph.as(NessGraph.class);
	}

	public DatalakeManager datalakeManager() {
		return datalakeManager;
	}

	public BusManager busManager() {
		return busManager;
	}
}