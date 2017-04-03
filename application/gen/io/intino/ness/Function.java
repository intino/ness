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

	public void source(java.lang.String value) {
		this.source = value;
	}

	@Override
	public java.util.Map<java.lang.String, java.util.List<?>> variables() {
		java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>();
		map.put("source", new java.util.ArrayList(java.util.Collections.singletonList(this.source)));
		return map;
	}

	public io.intino.tara.magritte.Concept concept() {
		return this.graph().concept(io.intino.ness.Function.class);
	}

	@Override
	protected void _load(java.lang.String name, java.util.List<?> values) {
		super._load(name, values);
		if (name.equalsIgnoreCase("source")) this.source = io.intino.tara.magritte.loaders.StringLoader.load(values, this).get(0);
	}

	@Override
	protected void _set(java.lang.String name, java.util.List<?> values) {
		super._set(name, values);
		if (name.equalsIgnoreCase("source")) this.source = (java.lang.String) values.get(0);
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
