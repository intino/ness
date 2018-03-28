package io.intino.ness.graph.topicsource;

import io.intino.ness.graph.*;


public class TopicSourcePipe extends io.intino.tara.magritte.Layer implements io.intino.tara.magritte.tags.Terminal {
	protected java.lang.String origin;
	protected io.intino.ness.graph.Pipe _pipe;

	public TopicSourcePipe(io.intino.tara.magritte.Node node) {
		super(node);
	}

	public java.lang.String origin() {
		return origin;
	}

	public io.intino.ness.graph.Tank destination() {
		return _pipe.destination();
	}

	public io.intino.ness.graph.Function transformer() {
		return _pipe.transformer();
	}

	public TopicSourcePipe origin(java.lang.String value) {
		this.origin = value;
		return (TopicSourcePipe) this;
	}

	public TopicSourcePipe destination(io.intino.ness.graph.Tank value) {
		this._pipe.destination(value);
		return (TopicSourcePipe) this;
	}

	public TopicSourcePipe transformer(io.intino.ness.graph.Function value) {
		this._pipe.transformer(value);
		return (TopicSourcePipe) this;
	}

	@Override
	protected java.util.Map<java.lang.String, java.util.List<?>> variables$() {
		java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>();
		map.put("origin", new java.util.ArrayList(java.util.Collections.singletonList(this.origin)));
		return map;
	}

	@Override
	protected void load$(java.lang.String name, java.util.List<?> values) {
		super.load$(name, values);
		if (name.equalsIgnoreCase("origin")) this.origin = io.intino.tara.magritte.loaders.StringLoader.load(values, this).get(0);
	}

	@Override
	protected void set$(java.lang.String name, java.util.List<?> values) {
		super.set$(name, values);
		if (name.equalsIgnoreCase("origin")) this.origin = (java.lang.String) values.get(0);
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
