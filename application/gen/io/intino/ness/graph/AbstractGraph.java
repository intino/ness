package io.intino.ness.graph;

import io.intino.tara.magritte.Graph;

public class AbstractGraph extends io.intino.tara.magritte.GraphWrapper {

	protected io.intino.tara.magritte.Graph graph;
	private java.util.List<io.intino.ness.graph.Function> functionList;
	private java.util.List<io.intino.ness.graph.Tank> tankList;
	private java.util.List<io.intino.ness.graph.Connection> connectionList;
	private java.util.List<io.intino.ness.graph.User> userList;
	private java.util.List<io.intino.ness.graph.ExternalBus> externalBusList;
	private java.util.List<io.intino.ness.graph.BusPipe> busPipeList;
	private java.util.List<io.intino.ness.graph.Pipe> pipeList;

	public AbstractGraph(io.intino.tara.magritte.Graph graph) {
		this.graph = graph;
		this.graph.i18n().register("ness");
	}

	public AbstractGraph(io.intino.tara.magritte.Graph graph, AbstractGraph wrapper) {
		this.graph = graph;
		this.graph.i18n().register("ness");
		this.functionList = new java.util.ArrayList<>(wrapper.functionList);
		this.tankList = new java.util.ArrayList<>(wrapper.tankList);
		this.connectionList = new java.util.ArrayList<>(wrapper.connectionList);
		this.userList = new java.util.ArrayList<>(wrapper.userList);
		this.externalBusList = new java.util.ArrayList<>(wrapper.externalBusList);
		this.busPipeList = new java.util.ArrayList<>(wrapper.busPipeList);
		this.pipeList = new java.util.ArrayList<>(wrapper.pipeList);
	}

    @Override
	public void update() {
		functionList = this.graph.rootList(io.intino.ness.graph.Function.class);
		tankList = this.graph.rootList(io.intino.ness.graph.Tank.class);
		connectionList = this.graph.rootList(io.intino.ness.graph.Connection.class);
		userList = this.graph.rootList(io.intino.ness.graph.User.class);
		externalBusList = this.graph.rootList(io.intino.ness.graph.ExternalBus.class);
		busPipeList = this.graph.rootList(io.intino.ness.graph.BusPipe.class);
		pipeList = this.graph.rootList(io.intino.ness.graph.Pipe.class);
	}

	@Override
	protected void addNode$(io.intino.tara.magritte.Node node) {
		if (node.is("Function")) this.functionList.add(node.as(io.intino.ness.graph.Function.class));
		if (node.is("Tank")) this.tankList.add(node.as(io.intino.ness.graph.Tank.class));
		if (node.is("Connection")) this.connectionList.add(node.as(io.intino.ness.graph.Connection.class));
		if (node.is("User")) this.userList.add(node.as(io.intino.ness.graph.User.class));
		if (node.is("ExternalBus")) this.externalBusList.add(node.as(io.intino.ness.graph.ExternalBus.class));
		if (node.is("BusPipe")) this.busPipeList.add(node.as(io.intino.ness.graph.BusPipe.class));
		if (node.is("Pipe")) this.pipeList.add(node.as(io.intino.ness.graph.Pipe.class));
	}

	@Override
	protected void removeNode$(io.intino.tara.magritte.Node node) {
		if (node.is("Function")) this.functionList.remove(node.as(io.intino.ness.graph.Function.class));
		if (node.is("Tank")) this.tankList.remove(node.as(io.intino.ness.graph.Tank.class));
		if (node.is("Connection")) this.connectionList.remove(node.as(io.intino.ness.graph.Connection.class));
		if (node.is("User")) this.userList.remove(node.as(io.intino.ness.graph.User.class));
		if (node.is("ExternalBus")) this.externalBusList.remove(node.as(io.intino.ness.graph.ExternalBus.class));
		if (node.is("BusPipe")) this.busPipeList.remove(node.as(io.intino.ness.graph.BusPipe.class));
		if (node.is("Pipe")) this.pipeList.remove(node.as(io.intino.ness.graph.Pipe.class));
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

	public java.util.List<io.intino.ness.graph.ExternalBus> externalBusList() {
		return externalBusList;
	}

	public java.util.List<io.intino.ness.graph.BusPipe> busPipeList() {
		return busPipeList;
	}

	public java.util.List<io.intino.ness.graph.Pipe> pipeList() {
		return pipeList;
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

	public java.util.stream.Stream<io.intino.ness.graph.ExternalBus> externalBusList(java.util.function.Predicate<io.intino.ness.graph.ExternalBus> filter) {
		return externalBusList.stream().filter(filter);
	}

	public io.intino.ness.graph.ExternalBus externalBus(int index) {
		return externalBusList.get(index);
	}

	public java.util.stream.Stream<io.intino.ness.graph.BusPipe> busPipeList(java.util.function.Predicate<io.intino.ness.graph.BusPipe> filter) {
		return busPipeList.stream().filter(filter);
	}

	public io.intino.ness.graph.BusPipe busPipe(int index) {
		return busPipeList.get(index);
	}

	public java.util.stream.Stream<io.intino.ness.graph.Pipe> pipeList(java.util.function.Predicate<io.intino.ness.graph.Pipe> filter) {
		return pipeList.stream().filter(filter);
	}

	public io.intino.ness.graph.Pipe pipe(int index) {
		return pipeList.get(index);
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

		public io.intino.ness.graph.Function function(java.lang.String qualifiedName, java.lang.String source, io.intino.ness.inl.MessageFunction aClass) {
			io.intino.ness.graph.Function newElement = AbstractGraph.this.graph.createRoot(io.intino.ness.graph.Function.class, stash, name).a$(io.intino.ness.graph.Function.class);
			newElement.core$().set(newElement, "qualifiedName", java.util.Collections.singletonList(qualifiedName));
			newElement.core$().set(newElement, "source", java.util.Collections.singletonList(source));
			newElement.core$().set(newElement, "aClass", java.util.Collections.singletonList(aClass));
			return newElement;
		}

		public io.intino.ness.graph.Tank tank(java.lang.String qualifiedName) {
			io.intino.ness.graph.Tank newElement = AbstractGraph.this.graph.createRoot(io.intino.ness.graph.Tank.class, stash, name).a$(io.intino.ness.graph.Tank.class);
			newElement.core$().set(newElement, "qualifiedName", java.util.Collections.singletonList(qualifiedName));
			return newElement;
		}

		public io.intino.ness.graph.Connection connection(io.intino.ness.graph.Tank faucet, io.intino.ness.graph.Tank flooder, io.intino.ness.graph.Function plug) {
			io.intino.ness.graph.Connection newElement = AbstractGraph.this.graph.createRoot(io.intino.ness.graph.Connection.class, stash, name).a$(io.intino.ness.graph.Connection.class);
			newElement.core$().set(newElement, "faucet", java.util.Collections.singletonList(faucet));
			newElement.core$().set(newElement, "flooder", java.util.Collections.singletonList(flooder));
			newElement.core$().set(newElement, "plug", java.util.Collections.singletonList(plug));
			return newElement;
		}

		public io.intino.ness.graph.User user(java.lang.String password, java.util.List<java.lang.String> groups) {
			io.intino.ness.graph.User newElement = AbstractGraph.this.graph.createRoot(io.intino.ness.graph.User.class, stash, name).a$(io.intino.ness.graph.User.class);
			newElement.core$().set(newElement, "password", java.util.Collections.singletonList(password));
			newElement.core$().set(newElement, "groups", groups);
			return newElement;
		}

		public io.intino.ness.graph.ExternalBus externalBus(java.lang.String originURL, java.lang.String sessionID, java.lang.String user, java.lang.String password) {
			io.intino.ness.graph.ExternalBus newElement = AbstractGraph.this.graph.createRoot(io.intino.ness.graph.ExternalBus.class, stash, name).a$(io.intino.ness.graph.ExternalBus.class);
			newElement.core$().set(newElement, "originURL", java.util.Collections.singletonList(originURL));
			newElement.core$().set(newElement, "sessionID", java.util.Collections.singletonList(sessionID));
			newElement.core$().set(newElement, "user", java.util.Collections.singletonList(user));
			newElement.core$().set(newElement, "password", java.util.Collections.singletonList(password));
			return newElement;
		}

		public io.intino.ness.graph.BusPipe busPipe(io.intino.ness.graph.BusPipe.Direction direction, io.intino.ness.graph.ExternalBus bus, io.intino.ness.graph.Function transformer, java.lang.String tankMacro) {
			io.intino.ness.graph.BusPipe newElement = AbstractGraph.this.graph.createRoot(io.intino.ness.graph.BusPipe.class, stash, name).a$(io.intino.ness.graph.BusPipe.class);
			newElement.core$().set(newElement, "direction", java.util.Collections.singletonList(direction));
			newElement.core$().set(newElement, "bus", java.util.Collections.singletonList(bus));
			newElement.core$().set(newElement, "transformer", java.util.Collections.singletonList(transformer));
			newElement.core$().set(newElement, "tankMacro", java.util.Collections.singletonList(tankMacro));
			return newElement;
		}

		public io.intino.ness.graph.Pipe pipe(java.lang.String origin, java.lang.String destination) {
			io.intino.ness.graph.Pipe newElement = AbstractGraph.this.graph.createRoot(io.intino.ness.graph.Pipe.class, stash, name).a$(io.intino.ness.graph.Pipe.class);
			newElement.core$().set(newElement, "origin", java.util.Collections.singletonList(origin));
			newElement.core$().set(newElement, "destination", java.util.Collections.singletonList(destination));
			return newElement;
		}
	}

	public class Clear {
	    public void function(java.util.function.Predicate<io.intino.ness.graph.Function> filter) {
	    	new java.util.ArrayList<>(AbstractGraph.this.functionList()).stream().filter(filter).forEach(io.intino.tara.magritte.Layer::delete$);
	    }

	    public void tank(java.util.function.Predicate<io.intino.ness.graph.Tank> filter) {
	    	new java.util.ArrayList<>(AbstractGraph.this.tankList()).stream().filter(filter).forEach(io.intino.tara.magritte.Layer::delete$);
	    }

	    public void connection(java.util.function.Predicate<io.intino.ness.graph.Connection> filter) {
	    	new java.util.ArrayList<>(AbstractGraph.this.connectionList()).stream().filter(filter).forEach(io.intino.tara.magritte.Layer::delete$);
	    }

	    public void user(java.util.function.Predicate<io.intino.ness.graph.User> filter) {
	    	new java.util.ArrayList<>(AbstractGraph.this.userList()).stream().filter(filter).forEach(io.intino.tara.magritte.Layer::delete$);
	    }

	    public void externalBus(java.util.function.Predicate<io.intino.ness.graph.ExternalBus> filter) {
	    	new java.util.ArrayList<>(AbstractGraph.this.externalBusList()).stream().filter(filter).forEach(io.intino.tara.magritte.Layer::delete$);
	    }

	    public void busPipe(java.util.function.Predicate<io.intino.ness.graph.BusPipe> filter) {
	    	new java.util.ArrayList<>(AbstractGraph.this.busPipeList()).stream().filter(filter).forEach(io.intino.tara.magritte.Layer::delete$);
	    }

	    public void pipe(java.util.function.Predicate<io.intino.ness.graph.Pipe> filter) {
	    	new java.util.ArrayList<>(AbstractGraph.this.pipeList()).stream().filter(filter).forEach(io.intino.tara.magritte.Layer::delete$);
	    }
	}
}