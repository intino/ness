package io.intino.ness.graph;

import io.intino.ness.graph.*;


public class Pipe extends io.intino.tara.magritte.Layer implements io.intino.tara.magritte.tags.Terminal {
	protected io.intino.ness.graph.Tank destination;
	protected io.intino.ness.graph.Function transformer;

	public Pipe(io.intino.tara.magritte.Node node) {
		super(node);
	}

	public io.intino.ness.graph.Tank destination() {
		return destination;
	}

	public io.intino.ness.graph.Function transformer() {
		return transformer;
	}

	public Pipe destination(io.intino.ness.graph.Tank value) {
		this.destination = value;
		return (Pipe) this;
	}

	public Pipe transformer(io.intino.ness.graph.Function value) {
		this.transformer = value;
		return (Pipe) this;
	}

	public io.intino.ness.graph.topicsource.TopicSourcePipe asTopicSource() {
		return a$(io.intino.ness.graph.topicsource.TopicSourcePipe.class);
	}

	public io.intino.ness.graph.topicsource.TopicSourcePipe asTopicSource(java.lang.String origin) {
		io.intino.ness.graph.topicsource.TopicSourcePipe newElement = core$().addFacet(io.intino.ness.graph.topicsource.TopicSourcePipe.class);
		newElement.core$().set(newElement, "origin", java.util.Collections.singletonList(origin));
	    return newElement;
	}

	public boolean isTopicSource() {
		return core$().is(io.intino.ness.graph.topicsource.TopicSourcePipe.class);
	}

	public void removeTopicSource() {
		core$().removeFacet(io.intino.ness.graph.topicsource.TopicSourcePipe.class);
	}

	public io.intino.ness.graph.tanksource.TankSourcePipe asTankSource() {
		return a$(io.intino.ness.graph.tanksource.TankSourcePipe.class);
	}

	public io.intino.ness.graph.tanksource.TankSourcePipe asTankSource(io.intino.ness.graph.Tank origin) {
		io.intino.ness.graph.tanksource.TankSourcePipe newElement = core$().addFacet(io.intino.ness.graph.tanksource.TankSourcePipe.class);
		newElement.core$().set(newElement, "origin", java.util.Collections.singletonList(origin));
	    return newElement;
	}

	public boolean isTankSource() {
		return core$().is(io.intino.ness.graph.tanksource.TankSourcePipe.class);
	}

	public void removeTankSource() {
		core$().removeFacet(io.intino.ness.graph.tanksource.TankSourcePipe.class);
	}

	@Override
	protected java.util.Map<java.lang.String, java.util.List<?>> variables$() {
		java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>();
		map.put("destination", this.destination != null ? new java.util.ArrayList(java.util.Collections.singletonList(this.destination)) : java.util.Collections.emptyList());
		map.put("transformer", this.transformer != null ? new java.util.ArrayList(java.util.Collections.singletonList(this.transformer)) : java.util.Collections.emptyList());
		return map;
	}

	@Override
	protected void load$(java.lang.String name, java.util.List<?> values) {
		super.load$(name, values);
		if (name.equalsIgnoreCase("destination")) this.destination = io.intino.tara.magritte.loaders.NodeLoader.load(values, io.intino.ness.graph.Tank.class, this).get(0);
		else if (name.equalsIgnoreCase("transformer")) this.transformer = io.intino.tara.magritte.loaders.NodeLoader.load(values, io.intino.ness.graph.Function.class, this).get(0);
	}

	@Override
	protected void set$(java.lang.String name, java.util.List<?> values) {
		super.set$(name, values);
		if (name.equalsIgnoreCase("destination")) this.destination = values.get(0)!= null ? core$().graph().load(((io.intino.tara.magritte.Layer) values.get(0)).core$().id()).as(io.intino.ness.graph.Tank.class) : null;
		else if (name.equalsIgnoreCase("transformer")) this.transformer = values.get(0)!= null ? core$().graph().load(((io.intino.tara.magritte.Layer) values.get(0)).core$().id()).as(io.intino.ness.graph.Function.class) : null;
	}


	public io.intino.ness.graph.NessGraph graph() {
		return (io.intino.ness.graph.NessGraph) core$().graph().as(io.intino.ness.graph.NessGraph.class);
	}
}
