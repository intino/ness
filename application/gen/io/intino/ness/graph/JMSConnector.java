package io.intino.ness.graph;

import io.intino.ness.graph.*;


public class JMSConnector extends io.intino.tara.magritte.Layer implements io.intino.tara.magritte.tags.Terminal {
	protected Direction direction;

	public enum Direction {
		outgoing, incoming;
	}
	protected io.intino.ness.graph.ExternalBus bus;
	protected java.util.List<java.lang.String> topics = new java.util.ArrayList<>();
	protected boolean enabled;

	public JMSConnector(io.intino.tara.magritte.Node node) {
		super(node);
	}

	public Direction direction() {
		return direction;
	}

	public io.intino.ness.graph.ExternalBus bus() {
		return bus;
	}

	public java.util.List<java.lang.String> topics() {
		return topics;
	}

	public java.lang.String topics(int index) {
		return topics.get(index);
	}

	public java.util.List<java.lang.String> topics(java.util.function.Predicate<java.lang.String> predicate) {
		return topics().stream().filter(predicate).collect(java.util.stream.Collectors.toList());
	}

	public boolean enabled() {
		return enabled;
	}

	public JMSConnector direction(io.intino.ness.graph.JMSConnector.Direction value) {
		this.direction = value;
		return (JMSConnector) this;
	}

	public JMSConnector bus(io.intino.ness.graph.ExternalBus value) {
		this.bus = value;
		return (JMSConnector) this;
	}

	public JMSConnector enabled(boolean value) {
		this.enabled = value;
		return (JMSConnector) this;
	}

	@Override
	protected java.util.Map<java.lang.String, java.util.List<?>> variables$() {
		java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>();
		map.put("direction", new java.util.ArrayList(java.util.Collections.singletonList(this.direction)));
		map.put("bus", this.bus != null ? new java.util.ArrayList(java.util.Collections.singletonList(this.bus)) : java.util.Collections.emptyList());
		map.put("topics", this.topics);
		map.put("enabled", new java.util.ArrayList(java.util.Collections.singletonList(this.enabled)));
		return map;
	}

	@Override
	protected void load$(java.lang.String name, java.util.List<?> values) {
		super.load$(name, values);
		if (name.equalsIgnoreCase("direction")) this.direction = io.intino.tara.magritte.loaders.WordLoader.load(values, Direction.class, this).get(0);
		else if (name.equalsIgnoreCase("bus")) this.bus = io.intino.tara.magritte.loaders.NodeLoader.load(values, io.intino.ness.graph.ExternalBus.class, this).get(0);
		else if (name.equalsIgnoreCase("topics")) this.topics = io.intino.tara.magritte.loaders.StringLoader.load(values, this);
		else if (name.equalsIgnoreCase("enabled")) this.enabled = io.intino.tara.magritte.loaders.BooleanLoader.load(values, this).get(0);
	}

	@Override
	protected void set$(java.lang.String name, java.util.List<?> values) {
		super.set$(name, values);
		if (name.equalsIgnoreCase("direction")) this.direction = (Direction) values.get(0);
		else if (name.equalsIgnoreCase("bus")) this.bus = values.get(0)!= null ? core$().graph().load(((io.intino.tara.magritte.Layer) values.get(0)).core$().id()).as(io.intino.ness.graph.ExternalBus.class) : null;
		else if (name.equalsIgnoreCase("topics")) this.topics = new java.util.ArrayList<>((java.util.List<java.lang.String>) values);
		else if (name.equalsIgnoreCase("enabled")) this.enabled = (java.lang.Boolean) values.get(0);
	}


	public io.intino.ness.graph.NessGraph graph() {
		return (io.intino.ness.graph.NessGraph) core$().graph().as(io.intino.ness.graph.NessGraph.class);
	}
}
