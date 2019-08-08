package org.example.datahubtest.box;

import io.intino.datahub.DataHub;
import io.intino.datahub.graph.NessGraph;
import io.intino.tara.magritte.Graph;

import java.io.File;

public class DataHubTestBox extends AbstractBox {

	private DataHub dataHub;

	public DataHubTestBox(String[] args) {
		super(args);
	}

	public DataHubTestBox(DataHubTestConfiguration configuration) {
		super(configuration);
	}

	@Override
	public io.intino.alexandria.core.Box put(Object o) {
		super.put(o);
		if (o instanceof Graph)
			dataHub = new DataHub(((Graph) o).as(NessGraph.class), new File(configuration.stagePath()));
		return this;
	}

	public io.intino.alexandria.core.Box open() {
		super.open();
		dataHub.start();
		return this;
	}

	public DataHub dataHub() {
		return dataHub;
	}

	public void close() {
		super.close();
	}
}