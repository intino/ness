package io.intino.ness.datahub.graph.custom;

import io.intino.ness.datahub.graph.*;
import io.intino.alexandria.datalake.Datalake;
import io.intino.ness.datahub.datalake.adapter.Context;

public class CustomAdapter extends io.intino.ness.datahub.graph.tanktype.TankTypeAdapter implements io.intino.tara.magritte.tags.Terminal {
	protected io.intino.ness.datahub.graph.functions.DatalakeAdapter adapt;

	public CustomAdapter(io.intino.tara.magritte.Node node) {
		super(node);
	}

	public void adapt(Datalake datalake, Context context) {
		 adapt.adapt(datalake, context);
	}

	public CustomAdapter adapt(io.intino.ness.datahub.graph.functions.DatalakeAdapter value) {
		this.adapt = io.intino.tara.magritte.loaders.FunctionLoader.load(adapt, this, io.intino.ness.datahub.graph.functions.DatalakeAdapter.class);
		return (CustomAdapter) this;
	}

	@Override
	protected java.util.Map<java.lang.String, java.util.List<?>> variables$() {
		java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>(super.variables$());
		map.put("adapt", this.adapt != null ? new java.util.ArrayList(java.util.Collections.singletonList(this.adapt)) : java.util.Collections.emptyList());
		return map;
	}

	@Override
	protected void load$(java.lang.String name, java.util.List<?> values) {
		super.load$(name, values);
		_adapter.core$().load(_adapter, name, values);
		if (name.equalsIgnoreCase("adapt")) this.adapt = io.intino.tara.magritte.loaders.FunctionLoader.load(values, this, io.intino.ness.datahub.graph.functions.DatalakeAdapter.class).get(0);
	}

	@Override
	protected void set$(java.lang.String name, java.util.List<?> values) {
		super.set$(name, values);
		_adapter.core$().set(_adapter, name, values);
		if (name.equalsIgnoreCase("adapt")) this.adapt = io.intino.tara.magritte.loaders.FunctionLoader.load(values.get(0), this, io.intino.ness.datahub.graph.functions.DatalakeAdapter.class);
	}

	public io.intino.ness.datahub.graph.NessGraph graph() {
		return (io.intino.ness.datahub.graph.NessGraph) core$().graph().as(io.intino.ness.datahub.graph.NessGraph.class);
	}
}