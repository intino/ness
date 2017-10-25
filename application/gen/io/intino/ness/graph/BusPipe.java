package io.intino.ness.graph;

import io.intino.ness.graph.*;


public class BusPipe extends io.intino.tara.magritte.Layer implements io.intino.tara.magritte.tags.Terminal {
	protected Direction direction;

	public enum Direction {
		outgoing, incoming;
	}
	protected io.intino.ness.graph.ExternalBus bus;
	protected io.intino.ness.graph.Function transformer;
	protected java.lang.String tankMacro;

	public BusPipe(io.intino.tara.magritte.Node node) {
		super(node);
	}

	public Direction direction() {
		return direction;
	}

	public io.intino.ness.graph.ExternalBus bus() {
		return bus;
	}

	public io.intino.ness.graph.Function transformer() {
		return transformer;
	}

	public java.lang.String tankMacro() {
		return tankMacro;
	}

	public BusPipe direction(io.intino.ness.graph.BusPipe.Direction value) {
		this.direction = value;
		return (BusPipe) this;
	}

	public BusPipe bus(io.intino.ness.graph.ExternalBus value) {
		this.bus = value;
		return (BusPipe) this;
	}

	public BusPipe transformer(io.intino.ness.graph.Function value) {
		this.transformer = value;
		return (BusPipe) this;
	}

	public BusPipe tankMacro(java.lang.String value) {
		this.tankMacro = value;
		return (BusPipe) this;
	}

	@Override
	protected java.util.Map<java.lang.String, java.util.List<?>> variables$() {
		java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>();
		map.put("direction", new java.util.ArrayList(java.util.Collections.singletonList(this.direction)));
		map.put("bus", this.bus != null ? new java.util.ArrayList(java.util.Collections.singletonList(this.bus)) : java.util.Collections.emptyList());
		map.put("transformer", this.transformer != null ? new java.util.ArrayList(java.util.Collections.singletonList(this.transformer)) : java.util.Collections.emptyList());
		map.put("tankMacro", new java.util.ArrayList(java.util.Collections.singletonList(this.tankMacro)));
		return map;
	}

	@Override
	protected void load$(java.lang.String name, java.util.List<?> values) {
		super.load$(name, values);
		if (name.equalsIgnoreCase("direction")) this.direction = io.intino.tara.magritte.loaders.WordLoader.load(values, Direction.class, this).get(0);
		else if (name.equalsIgnoreCase("bus")) this.bus = io.intino.tara.magritte.loaders.NodeLoader.load(values, io.intino.ness.graph.ExternalBus.class, this).get(0);
		else if (name.equalsIgnoreCase("transformer")) this.transformer = io.intino.tara.magritte.loaders.NodeLoader.load(values, io.intino.ness.graph.Function.class, this).get(0);
		else if (name.equalsIgnoreCase("tankMacro")) this.tankMacro = io.intino.tara.magritte.loaders.StringLoader.load(values, this).get(0);
	}

	@Override
	protected void set$(java.lang.String name, java.util.List<?> values) {
		super.set$(name, values);
		if (name.equalsIgnoreCase("direction")) this.direction = (Direction) values.get(0);
		else if (name.equalsIgnoreCase("bus")) this.bus = values.get(0)!= null ? core$().graph().load(((io.intino.tara.magritte.Layer) values.get(0)).core$().id()).as(io.intino.ness.graph.ExternalBus.class) : null;
		else if (name.equalsIgnoreCase("transformer")) this.transformer = values.get(0)!= null ? core$().graph().load(((io.intino.tara.magritte.Layer) values.get(0)).core$().id()).as(io.intino.ness.graph.Function.class) : null;
		else if (name.equalsIgnoreCase("tankMacro")) this.tankMacro = (java.lang.String) values.get(0);
	}


	public io.intino.ness.graph.NessGraph graph() {
		return (io.intino.ness.graph.NessGraph) core$().graph().as(io.intino.ness.graph.NessGraph.class);
	}
}
