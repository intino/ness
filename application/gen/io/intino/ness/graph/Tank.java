package io.intino.ness.graph;

import io.intino.ness.graph.*;


public class Tank extends io.intino.tara.magritte.Layer implements io.intino.tara.magritte.tags.Terminal {
	protected java.lang.String qualifiedName;
	protected io.intino.tara.magritte.Expression<java.lang.String> feedQN;
	protected io.intino.tara.magritte.Expression<java.lang.String> flowQN;
	protected io.intino.tara.magritte.Expression<java.lang.String> dropQN;
	protected java.util.List<java.lang.String> tags = new java.util.ArrayList<>();
	protected io.intino.tara.magritte.Expression<java.lang.Integer> version;

	public Tank(io.intino.tara.magritte.Node node) {
		super(node);
	}

	public java.lang.String qualifiedName() {
		return qualifiedName;
	}

	public java.lang.String feedQN() {
		return feedQN.value();
	}

	public java.lang.String flowQN() {
		return flowQN.value();
	}

	public java.lang.String dropQN() {
		return dropQN.value();
	}

	public java.util.List<java.lang.String> tags() {
		return tags;
	}

	public java.lang.String tags(int index) {
		return tags.get(index);
	}

	public java.util.List<java.lang.String> tags(java.util.function.Predicate<java.lang.String> predicate) {
		return tags().stream().filter(predicate).collect(java.util.stream.Collectors.toList());
	}

	public int version() {
		return version.value();
	}

	public Tank qualifiedName(java.lang.String value) {
		this.qualifiedName = value;
		return (Tank) this;
	}

	public Tank feedQN(io.intino.tara.magritte.Expression<java.lang.String> value) {
		this.feedQN = io.intino.tara.magritte.loaders.FunctionLoader.load(value, this, io.intino.tara.magritte.Expression.class);
		return (Tank) this;
	}

	public Tank flowQN(io.intino.tara.magritte.Expression<java.lang.String> value) {
		this.flowQN = io.intino.tara.magritte.loaders.FunctionLoader.load(value, this, io.intino.tara.magritte.Expression.class);
		return (Tank) this;
	}

	public Tank dropQN(io.intino.tara.magritte.Expression<java.lang.String> value) {
		this.dropQN = io.intino.tara.magritte.loaders.FunctionLoader.load(value, this, io.intino.tara.magritte.Expression.class);
		return (Tank) this;
	}

	public Tank version(io.intino.tara.magritte.Expression<java.lang.Integer> value) {
		this.version = io.intino.tara.magritte.loaders.FunctionLoader.load(value, this, io.intino.tara.magritte.Expression.class);
		return (Tank) this;
	}

	@Override
	protected java.util.Map<java.lang.String, java.util.List<?>> variables$() {
		java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>();
		map.put("qualifiedName", new java.util.ArrayList(java.util.Collections.singletonList(this.qualifiedName)));
		map.put("feedQN", new java.util.ArrayList(java.util.Collections.singletonList(this.feedQN)));
		map.put("flowQN", new java.util.ArrayList(java.util.Collections.singletonList(this.flowQN)));
		map.put("dropQN", new java.util.ArrayList(java.util.Collections.singletonList(this.dropQN)));
		map.put("tags", this.tags);
		map.put("version", new java.util.ArrayList(java.util.Collections.singletonList(this.version)));
		return map;
	}

	@Override
	protected void load$(java.lang.String name, java.util.List<?> values) {
		super.load$(name, values);
		if (name.equalsIgnoreCase("qualifiedName")) this.qualifiedName = io.intino.tara.magritte.loaders.StringLoader.load(values, this).get(0);
		else if (name.equalsIgnoreCase("feedQN")) this.feedQN = io.intino.tara.magritte.loaders.FunctionLoader.load(values, this, io.intino.tara.magritte.Expression.class).get(0);
		else if (name.equalsIgnoreCase("flowQN")) this.flowQN = io.intino.tara.magritte.loaders.FunctionLoader.load(values, this, io.intino.tara.magritte.Expression.class).get(0);
		else if (name.equalsIgnoreCase("dropQN")) this.dropQN = io.intino.tara.magritte.loaders.FunctionLoader.load(values, this, io.intino.tara.magritte.Expression.class).get(0);
		else if (name.equalsIgnoreCase("tags")) this.tags = io.intino.tara.magritte.loaders.StringLoader.load(values, this);
		else if (name.equalsIgnoreCase("version")) this.version = io.intino.tara.magritte.loaders.FunctionLoader.load(values, this, io.intino.tara.magritte.Expression.class).get(0);
	}

	@Override
	protected void set$(java.lang.String name, java.util.List<?> values) {
		super.set$(name, values);
		if (name.equalsIgnoreCase("qualifiedName")) this.qualifiedName = (java.lang.String) values.get(0);
		else if (name.equalsIgnoreCase("feedQN")) this.feedQN = io.intino.tara.magritte.loaders.FunctionLoader.load(values.get(0), this, io.intino.tara.magritte.Expression.class);
		else if (name.equalsIgnoreCase("flowQN")) this.flowQN = io.intino.tara.magritte.loaders.FunctionLoader.load(values.get(0), this, io.intino.tara.magritte.Expression.class);
		else if (name.equalsIgnoreCase("dropQN")) this.dropQN = io.intino.tara.magritte.loaders.FunctionLoader.load(values.get(0), this, io.intino.tara.magritte.Expression.class);
		else if (name.equalsIgnoreCase("tags")) this.tags = new java.util.ArrayList<>((java.util.List<java.lang.String>) values);
		else if (name.equalsIgnoreCase("version")) this.version = io.intino.tara.magritte.loaders.FunctionLoader.load(values.get(0), this, io.intino.tara.magritte.Expression.class);
	}


	public io.intino.ness.graph.NessGraph graph() {
		return (io.intino.ness.graph.NessGraph) core$().graph().as(io.intino.ness.graph.NessGraph.class);
	}
}
