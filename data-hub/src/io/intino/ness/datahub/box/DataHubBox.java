package io.intino.ness.datahub.box;

import io.intino.alexandria.datalake.Datalake;
import io.intino.ness.datahub.DataHub;
import io.intino.ness.datahub.graph.NessGraph;

import java.io.File;

public class DataHubBox extends AbstractBox {

	private NessGraph graph;
	private DataHub dataHub;

	public DataHubBox(String[] args) {
		super(args);
	}

	public DataHubBox(DataHubConfiguration configuration) {
		super(configuration);
	}

	@Override
	public io.intino.alexandria.core.Box put(Object o) {
		if (o instanceof NessGraph) this.graph = (NessGraph) o;
		return this;
	}

	public io.intino.alexandria.core.Box open() {
		this.dataHub = new DataHub(graph, configuration.workspace());
		return super.open();
	}

	public void close() {
		super.close();
	}

	public Datalake datalake() {
		return dataHub.datalake();
	}

	public DataHub datahub() {
		return dataHub;
	}

	public NessGraph graph() {
		return graph;
	}

	public File adaptersFolder() {
		return new File(configuration.workspace(), "adapters");
	}
}