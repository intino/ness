package io.intino.ness.graph;

import io.intino.ness.graph.*;


public class User extends io.intino.tara.magritte.Layer implements io.intino.tara.magritte.tags.Terminal {
	protected java.lang.String password;
	protected java.util.List<java.lang.String> groups = new java.util.ArrayList<>();

	public User(io.intino.tara.magritte.Node node) {
		super(node);
	}

	public java.lang.String password() {
		return password;
	}

	public java.util.List<java.lang.String> groups() {
		return groups;
	}

	public java.lang.String groups(int index) {
		return groups.get(index);
	}

	public java.util.List<java.lang.String> groups(java.util.function.Predicate<java.lang.String> predicate) {
		return groups().stream().filter(predicate).collect(java.util.stream.Collectors.toList());
	}

	public User password(java.lang.String value) {
		this.password = value;
		return (User) this;
	}

	@Override
	protected java.util.Map<java.lang.String, java.util.List<?>> variables$() {
		java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>();
		map.put("password", new java.util.ArrayList(java.util.Collections.singletonList(this.password)));
		map.put("groups", this.groups);
		return map;
	}

	@Override
	protected void load$(java.lang.String name, java.util.List<?> values) {
		super.load$(name, values);
		if (name.equalsIgnoreCase("password")) this.password = io.intino.tara.magritte.loaders.StringLoader.load(values, this).get(0);
		else if (name.equalsIgnoreCase("groups")) this.groups = io.intino.tara.magritte.loaders.StringLoader.load(values, this);
	}

	@Override
	protected void set$(java.lang.String name, java.util.List<?> values) {
		super.set$(name, values);
		if (name.equalsIgnoreCase("password")) this.password = (java.lang.String) values.get(0);
		else if (name.equalsIgnoreCase("groups")) this.groups = new java.util.ArrayList<>((java.util.List<java.lang.String>) values);
	}


	public io.intino.ness.graph.NessGraph graph() {
		return (io.intino.ness.graph.NessGraph) core$().graph().as(io.intino.ness.graph.NessGraph.class);
	}
}
