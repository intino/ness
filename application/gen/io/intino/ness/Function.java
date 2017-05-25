package io.intino.ness;

import io.intino.ness.*;


public class Function extends io.intino.tara.magritte.Layer implements io.intino.tara.magritte.tags.Terminal {
	protected java.lang.String source;

	public Function(io.intino.tara.magritte.Node node) {
		super(node);
	}

	public java.lang.String source() {
		return source;
	}

	public Function source(java.lang.String value) {
		this.source = value;
		return (Function) this;
	}

	@Override
	protected java.util.Map<java.lang.String, java.util.List<?>> variables$() {
		java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>();
		map.put("source", new java.util.ArrayList(java.util.Collections.singletonList(this.source)));
		return map;
	}

	@Override
	protected void load$(java.lang.String name, java.util.List<?> values) {
		super.load$(name, values);
		if (name.equalsIgnoreCase("source")) this.source = io.intino.tara.magritte.loaders.StringLoader.load(values, this).get(0);
	}

	@Override
	protected void set$(java.lang.String name, java.util.List<?> values) {
		super.set$(name, values);
		if (name.equalsIgnoreCase("source")) this.source = (java.lang.String) values.get(0);
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
	
	public io.intino.ness.NessGraph graph() {
		return (io.intino.ness.NessGraph) core$().graph().as(io.intino.ness.NessGraph.class);
	}
}
