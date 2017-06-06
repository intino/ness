package io.intino.ness.graph;

import io.intino.tara.magritte.Graph;

public class AbstractGraph extends io.intino.tara.magritte.GraphWrapper {

	protected io.intino.tara.magritte.Graph graph;
	private java.util.List<io.intino.ness.graph.Function> functionList;
	private java.util.List<io.intino.ness.graph.Tank> tankList;
	private java.util.List<io.intino.ness.graph.Connection> connectionList;
	private java.util.List<io.intino.ness.graph.User> userList;

	public AbstractGraph(io.intino.tara.magritte.Graph graph) {
		this.graph = graph;
		this.graph.i18n().register("ness");
	}

	public void update() {
		functionList = this.graph.rootList(io.intino.ness.graph.Function.class);
		tankList = this.graph.rootList(io.intino.ness.graph.Tank.class);
		connectionList = this.graph.rootList(io.intino.ness.graph.Connection.class);
		userList = this.graph.rootList(io.intino.ness.graph.User.class);
	}

	@Override
	protected void addNode$(io.intino.tara.magritte.Node node) {
		if (node.is("Function")) this.functionList.add(node.as(io.intino.ness.graph.Function.class));
		if (node.is("Tank")) this.tankList.add(node.as(io.intino.ness.graph.Tank.class));
		if (node.is("Connection")) this.connectionList.add(node.as(io.intino.ness.graph.Connection.class));
		if (node.is("User")) this.userList.add(node.as(io.intino.ness.graph.User.class));
	}

	@Override
	protected void removeNode$(io.intino.tara.magritte.Node node) {
		if (node.is("Function")) this.functionList.remove(node.as(io.intino.ness.graph.Function.class));
		if (node.is("Tank")) this.tankList.remove(node.as(io.intino.ness.graph.Tank.class));
		if (node.is("Connection")) this.connectionList.remove(node.as(io.intino.ness.graph.Connection.class));
		if (node.is("User")) this.userList.remove(node.as(io.intino.ness.graph.User.class));
	}

	public java.net.URL resourceAsMessage$(String language, String key) {
		return graph.loadResource(graph.i18n().message(language, key));
	}

	public java.util.List<io.intino.ness.graph.Function> functionList() {
		return functionList;
	}

	public java.util.List<io.intino.ness.graph.Tank> tankList() {
		return tankList;
	}

	public java.util.List<io.intino.ness.graph.Connection> connectionList() {
		return connectionList;
	}

	public java.util.List<io.intino.ness.graph.User> userList() {
		return userList;
	}

	public java.util.stream.Stream<io.intino.ness.graph.Function> functionList(java.util.function.Predicate<io.intino.ness.graph.Function> filter) {
		return functionList.stream().filter(filter);
	}

	public io.intino.ness.graph.Function function(int index) {
		return functionList.get(index);
	}

	public java.util.stream.Stream<io.intino.ness.graph.Tank> tankList(java.util.function.Predicate<io.intino.ness.graph.Tank> filter) {
		return tankList.stream().filter(filter);
	}

	public io.intino.ness.graph.Tank tank(int index) {
		return tankList.get(index);
	}

	public java.util.stream.Stream<io.intino.ness.graph.Connection> connectionList(java.util.function.Predicate<io.intino.ness.graph.Connection> filter) {
		return connectionList.stream().filter(filter);
	}

	public io.intino.ness.graph.Connection connection(int index) {
		return connectionList.get(index);
	}

	public java.util.stream.Stream<io.intino.ness.graph.User> userList(java.util.function.Predicate<io.intino.ness.graph.User> filter) {
		return userList.stream().filter(filter);
	}

	public io.intino.ness.graph.User user(int index) {
		return userList.get(index);
	}

	public io.intino.tara.magritte.Graph core$() {
		return graph;
	}

	public io.intino.tara.magritte.utils.I18n i18n$() {
		return graph.i18n();
	}

	public Create create() {
		return new Create("Misc", null);
	}

	public Create create(String stash) {
		return new Create(stash, null);
	}

	public Create create(String stash, String name) {
		return new Create(stash, name);
	}

	public Clear clear() {
		return new Clear();
	}

	public class Create {
		private final String stash;
		private final String name;

		public Create(String stash, String name) {
			this.stash = stash;
			this.name = name;
		}

		public io.intino.ness.graph.Function function(java.lang.String source) {
			io.intino.ness.graph.Function newElement = AbstractGraph.this.graph.createRoot(io.intino.ness.graph.Function.class, stash, name).core$().as(io.intino.ness.graph.Function.class);
			newElement.core$().set(newElement, "source", java.util.Collections.singletonList(source));
			return newElement;
		}

		public io.intino.ness.graph.Tank tank(java.lang.String qualifiedName) {
			io.intino.ness.graph.Tank newElement = AbstractGraph.this.graph.createRoot(io.intino.ness.graph.Tank.class, stash, name).core$().as(io.intino.ness.graph.Tank.class);
			newElement.core$().set(newElement, "qualifiedName", java.util.Collections.singletonList(qualifiedName));
			return newElement;
		}

		public io.intino.ness.graph.Connection connection(io.intino.ness.graph.Tank faucet, io.intino.ness.graph.Tank flooder, io.intino.ness.graph.Function plug) {
			io.intino.ness.graph.Connection newElement = AbstractGraph.this.graph.createRoot(io.intino.ness.graph.Connection.class, stash, name).core$().as(io.intino.ness.graph.Connection.class);
			newElement.core$().set(newElement, "faucet", java.util.Collections.singletonList(faucet));
			newElement.core$().set(newElement, "flooder", java.util.Collections.singletonList(flooder));
			newElement.core$().set(newElement, "plug", java.util.Collections.singletonList(plug));
			return newElement;
		}

		public io.intino.ness.graph.User user(java.lang.String password, java.util.List<java.lang.String> groups) {
			io.intino.ness.graph.User newElement = AbstractGraph.this.graph.createRoot(io.intino.ness.graph.User.class, stash, name).core$().as(io.intino.ness.graph.User.class);
			newElement.core$().set(newElement, "password", java.util.Collections.singletonList(password));
			newElement.core$().set(newElement, "groups", groups);
			return newElement;
		}
	}

	public class Clear {
	    public void function(java.util.function.Predicate<io.intino.ness.graph.Function>... filters) {
	    	new java.util.ArrayList<>(AbstractGraph.this.functionList()).forEach(io.intino.tara.magritte.Layer::delete$);
	    }

	    public void tank(java.util.function.Predicate<io.intino.ness.graph.Tank>... filters) {
	    	new java.util.ArrayList<>(AbstractGraph.this.tankList()).forEach(io.intino.tara.magritte.Layer::delete$);
	    }

	    public void connection(java.util.function.Predicate<io.intino.ness.graph.Connection>... filters) {
	    	new java.util.ArrayList<>(AbstractGraph.this.connectionList()).forEach(io.intino.tara.magritte.Layer::delete$);
	    }

	    public void user(java.util.function.Predicate<io.intino.ness.graph.User>... filters) {
	    	new java.util.ArrayList<>(AbstractGraph.this.userList()).forEach(io.intino.tara.magritte.Layer::delete$);
	    }
	}
}