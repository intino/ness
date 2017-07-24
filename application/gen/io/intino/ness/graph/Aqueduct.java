package io.intino.ness.graph;

import io.intino.ness.graph.*;


public class Aqueduct extends io.intino.tara.magritte.Layer implements io.intino.tara.magritte.tags.Terminal {
	protected java.lang.String originURL;
	protected java.lang.String user;
	protected java.lang.String password;
	protected java.lang.String originTopic;
	protected java.lang.String destinationTopic;
	protected io.intino.ness.graph.Function transformer;

	public Aqueduct(io.intino.tara.magritte.Node node) {
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

	public java.lang.String originTopic() {
		return originTopic;
	}

	public java.lang.String destinationTopic() {
		return destinationTopic;
	}

	public io.intino.ness.graph.Function transformer() {
		return transformer;
	}

	public Aqueduct originURL(java.lang.String value) {
		this.originURL = value;
		return (Aqueduct) this;
	}

	public Aqueduct user(java.lang.String value) {
		this.user = value;
		return (Aqueduct) this;
	}

	public Aqueduct password(java.lang.String value) {
		this.password = value;
		return (Aqueduct) this;
	}

	public Aqueduct originTopic(java.lang.String value) {
		this.originTopic = value;
		return (Aqueduct) this;
	}

	public Aqueduct destinationTopic(java.lang.String value) {
		this.destinationTopic = value;
		return (Aqueduct) this;
	}

	public Aqueduct transformer(io.intino.ness.graph.Function value) {
		this.transformer = value;
		return (Aqueduct) this;
	}

	@Override
	protected java.util.Map<java.lang.String, java.util.List<?>> variables$() {
		java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>();
		map.put("originURL", new java.util.ArrayList(java.util.Collections.singletonList(this.originURL)));
		map.put("user", new java.util.ArrayList(java.util.Collections.singletonList(this.user)));
		map.put("password", new java.util.ArrayList(java.util.Collections.singletonList(this.password)));
		map.put("originTopic", new java.util.ArrayList(java.util.Collections.singletonList(this.originTopic)));
		map.put("destinationTopic", new java.util.ArrayList(java.util.Collections.singletonList(this.destinationTopic)));
		map.put("transformer", this.transformer != null ? new java.util.ArrayList(java.util.Collections.singletonList(this.transformer)) : java.util.Collections.emptyList());
		return map;
	}

	@Override
	protected void load$(java.lang.String name, java.util.List<?> values) {
		super.load$(name, values);
		if (name.equalsIgnoreCase("originURL")) this.originURL = io.intino.tara.magritte.loaders.StringLoader.load(values, this).get(0);
		else if (name.equalsIgnoreCase("user")) this.user = io.intino.tara.magritte.loaders.StringLoader.load(values, this).get(0);
		else if (name.equalsIgnoreCase("password")) this.password = io.intino.tara.magritte.loaders.StringLoader.load(values, this).get(0);
		else if (name.equalsIgnoreCase("originTopic")) this.originTopic = io.intino.tara.magritte.loaders.StringLoader.load(values, this).get(0);
		else if (name.equalsIgnoreCase("destinationTopic")) this.destinationTopic = io.intino.tara.magritte.loaders.StringLoader.load(values, this).get(0);
		else if (name.equalsIgnoreCase("transformer")) this.transformer = io.intino.tara.magritte.loaders.NodeLoader.load(values, io.intino.ness.graph.Function.class, this).get(0);
	}

	@Override
	protected void set$(java.lang.String name, java.util.List<?> values) {
		super.set$(name, values);
		if (name.equalsIgnoreCase("originURL")) this.originURL = (java.lang.String) values.get(0);
		else if (name.equalsIgnoreCase("user")) this.user = (java.lang.String) values.get(0);
		else if (name.equalsIgnoreCase("password")) this.password = (java.lang.String) values.get(0);
		else if (name.equalsIgnoreCase("originTopic")) this.originTopic = (java.lang.String) values.get(0);
		else if (name.equalsIgnoreCase("destinationTopic")) this.destinationTopic = (java.lang.String) values.get(0);
		else if (name.equalsIgnoreCase("transformer")) this.transformer = values.get(0)!= null ? core$().graph().load(((io.intino.tara.magritte.Layer) values.get(0)).core$().id()).as(io.intino.ness.graph.Function.class) : null;
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
