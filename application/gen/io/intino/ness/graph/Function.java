package io.intino.ness.graph;

import io.intino.ness.graph.*;


public class Function extends io.intino.tara.magritte.Layer implements io.intino.tara.magritte.tags.Terminal {
	protected java.lang.String qualifiedName;
	protected java.lang.String source;

	public Function(io.intino.tara.magritte.Node node) {
		super(node);
	}

	public java.lang.String qualifiedName() {
		return qualifiedName;
	}

	public java.lang.String source() {
		return source;
	}

	public Function qualifiedName(java.lang.String value) {
		this.qualifiedName = value;
		return (Function) this;
	}

	public Function source(java.lang.String value) {
		this.source = value;
		return (Function) this;
	}

	@Override
	protected java.util.Map<java.lang.String, java.util.List<?>> variables$() {
		java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>();
		map.put("qualifiedName", new java.util.ArrayList(java.util.Collections.singletonList(this.qualifiedName)));
		map.put("source", new java.util.ArrayList(java.util.Collections.singletonList(this.source)));
		return map;
	}

	@Override
	protected void load$(java.lang.String name, java.util.List<?> values) {
		super.load$(name, values);
		if (name.equalsIgnoreCase("qualifiedName")) this.qualifiedName = io.intino.tara.magritte.loaders.StringLoader.load(values, this).get(0);
		else if (name.equalsIgnoreCase("source")) this.source = io.intino.tara.magritte.loaders.StringLoader.load(values, this).get(0);
	}

	@Override
	protected void set$(java.lang.String name, java.util.List<?> values) {
		super.set$(name, values);
		if (name.equalsIgnoreCase("qualifiedName")) this.qualifiedName = (java.lang.String) values.get(0);
		else if (name.equalsIgnoreCase("source")) this.source = (java.lang.String) values.get(0);
	}


	public io.intino.ness.graph.NessGraph graph() {
		return (io.intino.ness.graph.NessGraph) core$().graph().as(io.intino.ness.graph.NessGraph.class);
	}
}
