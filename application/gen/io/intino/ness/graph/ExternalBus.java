package io.intino.ness.graph;

import io.intino.ness.graph.*;


public class ExternalBus extends io.intino.tara.magritte.Layer implements io.intino.tara.magritte.tags.Terminal {
	protected java.lang.String originURL;
	protected java.lang.String user;
	protected java.lang.String password;

	public ExternalBus(io.intino.tara.magritte.Node node) {
		super(node);
	}

	public java.lang.String originURL() {
		return originURL;
	}

	public java.lang.String user() {
		return user;
	}

	public java.lang.String password() {
		return password;
	}

	public ExternalBus originURL(java.lang.String value) {
		this.originURL = value;
		return (ExternalBus) this;
	}

	public ExternalBus user(java.lang.String value) {
		this.user = value;
		return (ExternalBus) this;
	}

	public ExternalBus password(java.lang.String value) {
		this.password = value;
		return (ExternalBus) this;
	}

	@Override
	protected java.util.Map<java.lang.String, java.util.List<?>> variables$() {
		java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>();
		map.put("originURL", new java.util.ArrayList(java.util.Collections.singletonList(this.originURL)));
		map.put("user", new java.util.ArrayList(java.util.Collections.singletonList(this.user)));
		map.put("password", new java.util.ArrayList(java.util.Collections.singletonList(this.password)));
		return map;
	}

	@Override
	protected void load$(java.lang.String name, java.util.List<?> values) {
		super.load$(name, values);
		if (name.equalsIgnoreCase("originURL")) this.originURL = io.intino.tara.magritte.loaders.StringLoader.load(values, this).get(0);
		else if (name.equalsIgnoreCase("user")) this.user = io.intino.tara.magritte.loaders.StringLoader.load(values, this).get(0);
		else if (name.equalsIgnoreCase("password")) this.password = io.intino.tara.magritte.loaders.StringLoader.load(values, this).get(0);
	}

	@Override
	protected void set$(java.lang.String name, java.util.List<?> values) {
		super.set$(name, values);
		if (name.equalsIgnoreCase("originURL")) this.originURL = (java.lang.String) values.get(0);
		else if (name.equalsIgnoreCase("user")) this.user = (java.lang.String) values.get(0);
		else if (name.equalsIgnoreCase("password")) this.password = (java.lang.String) values.get(0);
	}

	public Create create() {
		return new Create(null);
	}

	public Create create(java.lang.String name) {
		return new Create(name);
	}

	public Clear clear() {
		return new Clear();
	}

	public class Create {
		protected final java.lang.String name;

		public Create(java.lang.String name) {
			this.name = name;
		}
		
	}

	public class Clear {
		
	}
	
	public io.intino.ness.graph.NessGraph graph() {
		return (io.intino.ness.graph.NessGraph) core$().graph().as(io.intino.ness.graph.NessGraph.class);
	}
}
