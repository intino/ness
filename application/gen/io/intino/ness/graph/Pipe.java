package io.intino.ness.graph;

import io.intino.ness.graph.*;


public class Pipe extends io.intino.tara.magritte.Layer implements io.intino.tara.magritte.tags.Terminal {
	protected java.lang.String origin;
	protected java.lang.String destination;

	public Pipe(io.intino.tara.magritte.Node node) {
		super(node);
	}

	public java.lang.String origin() {
		return origin;
	}

	public java.lang.String destination() {
		return destination;
	}

	public Pipe origin(java.lang.String value) {
		this.origin = value;
		return (Pipe) this;
	}

	public Pipe destination(java.lang.String value) {
		this.destination = value;
		return (Pipe) this;
	}

	@Override
	protected java.util.Map<java.lang.String, java.util.List<?>> variables$() {
		java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>();
		map.put("origin", new java.util.ArrayList(java.util.Collections.singletonList(this.origin)));
		map.put("destination", new java.util.ArrayList(java.util.Collections.singletonList(this.destination)));
		return map;
	}

	@Override
	protected void load$(java.lang.String name, java.util.List<?> values) {
		super.load$(name, values);
		if (name.equalsIgnoreCase("origin")) this.origin = io.intino.tara.magritte.loaders.StringLoader.load(values, this).get(0);
		else if (name.equalsIgnoreCase("destination")) this.destination = io.intino.tara.magritte.loaders.StringLoader.load(values, this).get(0);
	}

	@Override
	protected void set$(java.lang.String name, java.util.List<?> values) {
		super.set$(name, values);
		if (name.equalsIgnoreCase("origin")) this.origin = (java.lang.String) values.get(0);
		else if (name.equalsIgnoreCase("destination")) this.destination = (java.lang.String) values.get(0);
	}


	public io.intino.ness.graph.NessGraph graph() {
		return (io.intino.ness.graph.NessGraph) core$().graph().as(io.intino.ness.graph.NessGraph.class);
	}
}
