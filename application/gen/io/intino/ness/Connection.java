package io.intino.ness;

import io.intino.ness.*;


public class Connection extends io.intino.tara.magritte.Layer implements io.intino.tara.magritte.tags.Terminal {
	protected io.intino.ness.Topic faucet;
	protected io.intino.ness.Topic flooder;
	protected io.intino.ness.Function plug;

	public Connection(io.intino.tara.magritte.Node node) {
		super(node);
	}

	public io.intino.ness.Topic faucet() {
		return faucet;
	}

	public io.intino.ness.Topic flooder() {
		return flooder;
	}

	public io.intino.ness.Function plug() {
		return plug;
	}

	public void faucet(io.intino.ness.Topic value) {
		this.faucet = value;
	}

	public void flooder(io.intino.ness.Topic value) {
		this.flooder = value;
	}

	public void plug(io.intino.ness.Function value) {
		this.plug = value;
	}

	@Override
	public java.util.Map<java.lang.String, java.util.List<?>> variables() {
		java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>();
		map.put("faucet", this.faucet != null ? new java.util.ArrayList(java.util.Collections.singletonList(this.faucet)) : java.util.Collections.emptyList());
		map.put("flooder", this.flooder != null ? new java.util.ArrayList(java.util.Collections.singletonList(this.flooder)) : java.util.Collections.emptyList());
		map.put("plug", this.plug != null ? new java.util.ArrayList(java.util.Collections.singletonList(this.plug)) : java.util.Collections.emptyList());
		return map;
	}

	public io.intino.tara.magritte.Concept concept() {
		return this.graph().concept(io.intino.ness.Connection.class);
	}

	@Override
	protected void _load(java.lang.String name, java.util.List<?> values) {
		super._load(name, values);
		if (name.equalsIgnoreCase("faucet")) this.faucet = io.intino.tara.magritte.loaders.NodeLoader.load(values, io.intino.ness.Topic.class, this).get(0);
		else if (name.equalsIgnoreCase("flooder")) this.flooder = io.intino.tara.magritte.loaders.NodeLoader.load(values, io.intino.ness.Topic.class, this).get(0);
		else if (name.equalsIgnoreCase("plug")) this.plug = io.intino.tara.magritte.loaders.NodeLoader.load(values, io.intino.ness.Function.class, this).get(0);
	}

	@Override
	protected void _set(java.lang.String name, java.util.List<?> values) {
		super._set(name, values);
		if (name.equalsIgnoreCase("faucet")) this.faucet = values.get(0)!= null ? graph().loadNode(((io.intino.tara.magritte.Layer) values.get(0)).id()).as(io.intino.ness.Topic.class) : null;
		else if (name.equalsIgnoreCase("flooder")) this.flooder = values.get(0)!= null ? graph().loadNode(((io.intino.tara.magritte.Layer) values.get(0)).id()).as(io.intino.ness.Topic.class) : null;
		else if (name.equalsIgnoreCase("plug")) this.plug = values.get(0)!= null ? graph().loadNode(((io.intino.tara.magritte.Layer) values.get(0)).id()).as(io.intino.ness.Function.class) : null;
	}

	public Create create() {
		return new Create(null);
	}

	public Create create(java.lang.String name) {
		return new Create(name);
	}

	public class Create {
		protected final java.lang.String name;

		public Create(java.lang.String name) {
			this.name = name;
		}
		
	}
	
	public io.intino.ness.Ness nessWrapper() {
		return (io.intino.ness.Ness) graph().wrapper(io.intino.ness.Ness.class);
	}
}
