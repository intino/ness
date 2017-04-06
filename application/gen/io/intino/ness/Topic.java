package io.intino.ness;

import io.intino.ness.*;


public class Topic extends io.intino.tara.magritte.Layer implements io.intino.tara.magritte.tags.Terminal {
	protected java.lang.String qualifiedName;
	protected java.util.List<java.lang.String> tags = new java.util.ArrayList<>();

	public Topic(io.intino.tara.magritte.Node node) {
		super(node);
	}

	public java.lang.String qualifiedName() {
		return qualifiedName;
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

	public void qualifiedName(java.lang.String value) {
		this.qualifiedName = value;
	}

	@Override
	public java.util.Map<java.lang.String, java.util.List<?>> variables() {
		java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>();
		map.put("qualifiedName", new java.util.ArrayList(java.util.Collections.singletonList(this.qualifiedName)));
		map.put("tags", this.tags);
		return map;
	}

	public io.intino.tara.magritte.Concept concept() {
		return this.graph().concept(io.intino.ness.Topic.class);
	}

	@Override
	protected void _load(java.lang.String name, java.util.List<?> values) {
		super._load(name, values);
		if (name.equalsIgnoreCase("qualifiedName")) this.qualifiedName = io.intino.tara.magritte.loaders.StringLoader.load(values, this).get(0);
		else if (name.equalsIgnoreCase("tags")) this.tags = io.intino.tara.magritte.loaders.StringLoader.load(values, this);
	}

	@Override
	protected void _set(java.lang.String name, java.util.List<?> values) {
		super._set(name, values);
		if (name.equalsIgnoreCase("qualifiedName")) this.qualifiedName = (java.lang.String) values.get(0);
		else if (name.equalsIgnoreCase("tags")) this.tags = new java.util.ArrayList<>((java.util.List<java.lang.String>) values);
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
