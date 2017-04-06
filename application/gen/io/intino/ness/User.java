package io.intino.ness;

import io.intino.ness.*;


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

	public void password(java.lang.String value) {
		this.password = value;
	}

	@Override
	public java.util.Map<java.lang.String, java.util.List<?>> variables() {
		java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>();
		map.put("password", new java.util.ArrayList(java.util.Collections.singletonList(this.password)));
		map.put("groups", this.groups);
		return map;
	}

	public io.intino.tara.magritte.Concept concept() {
		return this.graph().concept(io.intino.ness.User.class);
	}

	@Override
	protected void _load(java.lang.String name, java.util.List<?> values) {
		super._load(name, values);
		if (name.equalsIgnoreCase("password")) this.password = io.intino.tara.magritte.loaders.StringLoader.load(values, this).get(0);
		else if (name.equalsIgnoreCase("groups")) this.groups = io.intino.tara.magritte.loaders.StringLoader.load(values, this);
	}

	@Override
	protected void _set(java.lang.String name, java.util.List<?> values) {
		super._set(name, values);
		if (name.equalsIgnoreCase("password")) this.password = (java.lang.String) values.get(0);
		else if (name.equalsIgnoreCase("groups")) this.groups = new java.util.ArrayList<>((java.util.List<java.lang.String>) values);
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
