package org.example.datahubtest.box;

import io.intino.datahub.DataHub;
import io.intino.datahub.graph.NessGraph;
import io.intino.magritte.framework.Graph;

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

	@Override
	protected void beforeStart() {

	}

	@Override
	protected void afterStart() {

	}

	@Override
	protected void beforeStop() {

	}

	@Override
	protected void afterStop() {

	}

	public DataHub dataHub() {
		return dataHub;
	}

}