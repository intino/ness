package io.intino.ness.datahub.graph.adaptertype;

import io.intino.ness.datahub.graph.*;

public abstract class AdapterTypeAdapter  extends io.intino.tara.magritte.Layer implements io.intino.tara.magritte.tags.Terminal {
	protected io.intino.ness.datahub.graph.Adapter _adapter;

	public AdapterTypeAdapter(io.intino.tara.magritte.Node node) {
		super(node);
	}

	@Override
	protected java.util.Map<java.lang.String, java.util.List<?>> variables$() {
		java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>();
		return map;
	}

	@Override
	protected void load$(java.lang.String name, java.util.List<?> values) {
		super.load$(name, values);
	}

	@Override
	protected void set$(java.lang.String name, java.util.List<?> values) {
		super.set$(name, values);
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