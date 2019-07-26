package io.intino.ness.datahub.graph.batch;

import io.intino.ness.datahub.graph.*;

public class BatchAdapter extends io.intino.ness.datahub.graph.adaptertype.AdapterTypeAdapter implements io.intino.tara.magritte.tags.Terminal {

	public BatchAdapter(io.intino.tara.magritte.Node node) {
		super(node);
	}

	@Override
	protected java.util.Map<java.lang.String, java.util.List<?>> variables$() {
		java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>(super.variables$());
		return map;
	}

	@Override
	protected void load$(java.lang.String name, java.util.List<?> values) {
		super.load$(name, values);
		_adapter.core$().load(_adapter, name, values);
	}

	@Override
	protected void set$(java.lang.String name, java.util.List<?> values) {
		super.set$(name, values);
		_adapter.core$().set(_adapter, name, values);
	}

	public io.intino.ness.datahub.graph.NessGraph graph() {
		return (io.intino.ness.datahub.graph.NessGraph) core$().graph().as(io.intino.ness.datahub.graph.NessGraph.class);
	}
}