package io.intino.ness.graph;

import io.intino.ness.graph.*;


public class Connection extends io.intino.tara.magritte.Layer implements io.intino.tara.magritte.tags.Terminal {
	protected io.intino.ness.graph.Tank faucet;
	protected io.intino.ness.graph.Tank flooder;
	protected io.intino.ness.graph.Function plug;

	public Connection(io.intino.tara.magritte.Node node) {
		super(node);
	}

	public io.intino.ness.graph.Tank faucet() {
		return faucet;
	}

	public io.intino.ness.graph.Tank flooder() {
		return flooder;
	}

	public io.intino.ness.graph.Function plug() {
		return plug;
	}

	public Connection faucet(io.intino.ness.graph.Tank value) {
		this.faucet = value;
		return (Connection) this;
	}

	public Connection flooder(io.intino.ness.graph.Tank value) {
		this.flooder = value;
		return (Connection) this;
	}

	public Connection plug(io.intino.ness.graph.Function value) {
		this.plug = value;
		return (Connection) this;
	}

	@Override
	protected java.util.Map<java.lang.String, java.util.List<?>> variables$() {
		java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>();
		map.put("faucet", this.faucet != null ? new java.util.ArrayList(java.util.Collections.singletonList(this.faucet)) : java.util.Collections.emptyList());
		map.put("flooder", this.flooder != null ? new java.util.ArrayList(java.util.Collections.singletonList(this.flooder)) : java.util.Collections.emptyList());
		map.put("plug", this.plug != null ? new java.util.ArrayList(java.util.Collections.singletonList(this.plug)) : java.util.Collections.emptyList());
		return map;
	}

	@Override
	protected void load$(java.lang.String name, java.util.List<?> values) {
		super.load$(name, values);
		if (name.equalsIgnoreCase("faucet")) this.faucet = io.intino.tara.magritte.loaders.NodeLoader.load(values, io.intino.ness.graph.Tank.class, this).get(0);
		else if (name.equalsIgnoreCase("flooder")) this.flooder = io.intino.tara.magritte.loaders.NodeLoader.load(values, io.intino.ness.graph.Tank.class, this).get(0);
		else if (name.equalsIgnoreCase("plug")) this.plug = io.intino.tara.magritte.loaders.NodeLoader.load(values, io.intino.ness.graph.Function.class, this).get(0);
	}

	@Override
	protected void set$(java.lang.String name, java.util.List<?> values) {
		super.set$(name, values);
		if (name.equalsIgnoreCase("faucet")) this.faucet = values.get(0)!= null ? core$().graph().load(((io.intino.tara.magritte.Layer) values.get(0)).core$().id()).as(io.intino.ness.graph.Tank.class) : null;
		else if (name.equalsIgnoreCase("flooder")) this.flooder = values.get(0)!= null ? core$().graph().load(((io.intino.tara.magritte.Layer) values.get(0)).core$().id()).as(io.intino.ness.graph.Tank.class) : null;
		else if (name.equalsIgnoreCase("plug")) this.plug = values.get(0)!= null ? core$().graph().load(((io.intino.tara.magritte.Layer) values.get(0)).core$().id()).as(io.intino.ness.graph.Function.class) : null;
	}


	public io.intino.ness.graph.NessGraph graph() {
		return (io.intino.ness.graph.NessGraph) core$().graph().as(io.intino.ness.graph.NessGraph.class);
	}
}
