package org.example.datahubtest.box;

public class DataHubTestBox extends AbstractBox {

	public DataHubTestBox(String[] args) {
		super(args);
	}

	public DataHubTestBox(DataHubTestConfiguration configuration) {
		super(configuration);
	}

	@Override
	public io.intino.alexandria.core.Box put(Object o) {
		super.put(o);
		return this;
	}

	public io.intino.alexandria.core.Box open() {
		return super.open();
	}

	public void close() {
		super.close();
	}
}