package io.intino.ness;

import io.intino.ness.*;


public class Topic extends io.intino.tara.magritte.Layer implements io.intino.tara.magritte.tags.Terminal {
	protected java.lang.String name$;
	protected Status status;

	public enum Status {
		Active, Inactive;
	}
	protected java.util.List<java.lang.String> tags = new java.util.ArrayList<>();

	public Topic(io.intino.tara.magritte.Node node) {
		super(node);
	}

	public java.lang.String name$() {
		return name$;
	}

	public Status status() {
		return status;
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

	public void name$(java.lang.String value) {
		this.name$ = value;
	}

	public void status(io.intino.ness.Topic.Status value) {
		this.status = value;
	}

	@Override
	public java.util.Map<java.lang.String, java.util.List<?>> variables() {
		java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>();
		map.put("name", new java.util.ArrayList(java.util.Collections.singletonList(this.name$)));
		map.put("status", new java.util.ArrayList(java.util.Collections.singletonList(this.status)));
		map.put("tags", this.tags);
		return map;
	}

	public io.intino.tara.magritte.Concept concept() {
		return this.graph().concept(io.intino.ness.Topic.class);
	}

	@Override
	protected void _load(java.lang.String name, java.util.List<?> values) {
		super._load(name, values);
		if (name.equalsIgnoreCase("name")) this.name$ = io.intino.tara.magritte.loaders.StringLoader.load(values, this).get(0);
		else if (name.equalsIgnoreCase("status")) this.status = io.intino.tara.magritte.loaders.WordLoader.load(values, Status.class, this).get(0);
		else if (name.equalsIgnoreCase("tags")) this.tags = io.intino.tara.magritte.loaders.StringLoader.load(values, this);
	}

	@Override
	protected void _set(java.lang.String name, java.util.List<?> values) {
		super._set(name, values);
		if (name.equalsIgnoreCase("name")) this.name$ = (java.lang.String) values.get(0);
		else if (name.equalsIgnoreCase("status")) this.status = (Status) values.get(0);
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
