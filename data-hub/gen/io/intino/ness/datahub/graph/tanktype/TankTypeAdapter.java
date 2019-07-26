package io.intino.ness.datahub.graph.tanktype;

import io.intino.ness.datahub.graph.*;
import io.intino.ness.datahub.datalake.adapter.Context;
import java.io.InputStream;

public abstract class TankTypeAdapter  extends io.intino.tara.magritte.Layer implements io.intino.tara.magritte.tags.Terminal {
	protected io.intino.ness.datahub.graph.functions.Configure configure;
	protected io.intino.ness.datahub.graph.Adapter _adapter;

	public TankTypeAdapter(io.intino.tara.magritte.Node node) {
		super(node);
	}

	public void configure(Context context, String configuration, InputStream attachment) {
		 configure.configure(context, configuration, attachment);
	}

	public TankTypeAdapter configure(io.intino.ness.datahub.graph.functions.Configure value) {
		this.configure = io.intino.tara.magritte.loaders.FunctionLoader.load(configure, this, io.intino.ness.datahub.graph.functions.Configure.class);
		return (TankTypeAdapter) this;
	}

	@Override
	protected java.util.Map<java.lang.String, java.util.List<?>> variables$() {
		java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>();
		map.put("configure", this.configure != null ? new java.util.ArrayList(java.util.Collections.singletonList(this.configure)) : java.util.Collections.emptyList());
		return map;
	}

	@Override
	protected void load$(java.lang.String name, java.util.List<?> values) {
		super.load$(name, values);
		if (name.equalsIgnoreCase("configure")) this.configure = io.intino.tara.magritte.loaders.FunctionLoader.load(values, this, io.intino.ness.datahub.graph.functions.Configure.class).get(0);
	}

	@Override
	protected void set$(java.lang.String name, java.util.List<?> values) {
		super.set$(name, values);
		if (name.equalsIgnoreCase("configure")) this.configure = io.intino.tara.magritte.loaders.FunctionLoader.load(values.get(0), this, io.intino.ness.datahub.graph.functions.Configure.class);
	}

	@Override
	protected void sync$(io.intino.tara.magritte.Layer layer) {
		super.sync$(layer);
	    if (layer instanceof io.intino.ness.datahub.graph.Adapter) _adapter = (io.intino.ness.datahub.graph.Adapter) layer;

	}

	public Create create() {
		return new Create(null);
	}

	public Create create(java.lang.String name) {
		return new Create(name);
	}

	public class Create  {
		protected final java.lang.String name;

		public Create(java.lang.String name) {
			this.name = name;
		}



	}

	public io.intino.ness.datahub.graph.NessGraph graph() {
		return (io.intino.ness.datahub.graph.NessGraph) core$().graph().as(io.intino.ness.datahub.graph.NessGraph.class);
	}
}