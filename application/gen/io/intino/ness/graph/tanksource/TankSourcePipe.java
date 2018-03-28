package io.intino.ness.graph.tanksource;

import io.intino.ness.graph.*;


public class TankSourcePipe extends io.intino.tara.magritte.Layer implements io.intino.tara.magritte.tags.Terminal {
	protected io.intino.ness.graph.Tank origin;
	protected io.intino.ness.graph.Pipe _pipe;

	public TankSourcePipe(io.intino.tara.magritte.Node node) {
		super(node);
	}

	public io.intino.ness.graph.Tank origin() {
		return origin;
	}

	public io.intino.ness.graph.Tank destination() {
		return _pipe.destination();
	}

	public io.intino.ness.graph.Function transformer() {
		return _pipe.transformer();
	}

	public TankSourcePipe origin(io.intino.ness.graph.Tank value) {
		this.origin = value;
		return (TankSourcePipe) this;
	}

	public TankSourcePipe destination(io.intino.ness.graph.Tank value) {
		this._pipe.destination(value);
		return (TankSourcePipe) this;
	}

	public TankSourcePipe transformer(io.intino.ness.graph.Function value) {
		this._pipe.transformer(value);
		return (TankSourcePipe) this;
	}

	@Override
	protected java.util.Map<java.lang.String, java.util.List<?>> variables$() {
		java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>();
		map.put("origin", this.origin != null ? new java.util.ArrayList(java.util.Collections.singletonList(this.origin)) : java.util.Collections.emptyList());
		return map;
	}

	@Override
	protected void load$(java.lang.String name, java.util.List<?> values) {
		super.load$(name, values);
		if (name.equalsIgnoreCase("origin")) this.origin = io.intino.tara.magritte.loaders.NodeLoader.load(values, io.intino.ness.graph.Tank.class, this).get(0);
	}

	@Override
	protected void set$(java.lang.String name, java.util.List<?> values) {
		super.set$(name, values);
		if (name.equalsIgnoreCase("origin")) this.origin = values.get(0)!= null ? core$().graph().load(((io.intino.tara.magritte.Layer) values.get(0)).core$().id()).as(io.intino.ness.graph.Tank.class) : null;
	}

	@Override
	protected void sync$(io.intino.tara.magritte.Layer layer) {
		super.sync$(layer);
	    if (layer instanceof io.intino.ness.graph.Pipe) _pipe = (io.intino.ness.graph.Pipe) layer;

	}


	public io.intino.ness.graph.NessGraph graph() {
		return (io.intino.ness.graph.NessGraph) core$().graph().as(io.intino.ness.graph.NessGraph.class);
	}
}
