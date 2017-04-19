package io.intino.ness;

import io.intino.tara.magritte.Graph;

public class GraphWrapper extends io.intino.tara.magritte.GraphWrapper {

	protected io.intino.tara.magritte.Graph graph;
	private java.util.List<io.intino.ness.Function> functionList;
	private java.util.List<io.intino.ness.Channel> channelList;
	private java.util.List<io.intino.ness.Connection> connectionList;
	private java.util.List<io.intino.ness.User> userList;

	public GraphWrapper(io.intino.tara.magritte.Graph graph) {
		this.graph = graph;
		this.graph.i18n().register("ness");
	}

	public void update() {
		functionList = this.graph.rootList(io.intino.ness.Function.class);
		channelList = this.graph.rootList(io.intino.ness.Channel.class);
		connectionList = this.graph.rootList(io.intino.ness.Connection.class);
		userList = this.graph.rootList(io.intino.ness.User.class);
	}

	@Override
	protected void addNode(io.intino.tara.magritte.Node node) {
		if (node.is("Function")) this.functionList.add(node.as(io.intino.ness.Function.class));
		if (node.is("Channel")) this.channelList.add(node.as(io.intino.ness.Channel.class));
		if (node.is("Connection")) this.connectionList.add(node.as(io.intino.ness.Connection.class));
		if (node.is("User")) this.userList.add(node.as(io.intino.ness.User.class));
	}

	@Override
	protected void removeNode(io.intino.tara.magritte.Node node) {
		if (node.is("Function")) this.functionList.remove(node.as(io.intino.ness.Function.class));
		if (node.is("Channel")) this.channelList.remove(node.as(io.intino.ness.Channel.class));
		if (node.is("Connection")) this.connectionList.remove(node.as(io.intino.ness.Connection.class));
		if (node.is("User")) this.userList.remove(node.as(io.intino.ness.User.class));
	}

	public String message(String language, String key, Object... parameters) {
		return graph.i18n().message(language, key, parameters);
	}

	public java.net.URL resourceAsMessage(String language, String key) {
		return graph.loadResource(graph.i18n().message(language, key));
	}

	public java.util.Map<String,String> keysIn(String language) {
		return graph.i18n().wordsIn(language);
	}

	public io.intino.tara.magritte.Concept concept(String concept) {
		return graph.concept(concept);
	}

	public io.intino.tara.magritte.Concept concept(java.lang.Class<? extends io.intino.tara.magritte.Layer> layerClass) {
		return graph.concept(layerClass);
	}

	public java.util.List<io.intino.tara.magritte.Concept> conceptList() {
		return graph.conceptList();
	}

	public java.util.List<io.intino.tara.magritte.Concept> conceptList(java.util.function.Predicate<io.intino.tara.magritte.Concept> predicate) {
		return graph.conceptList(predicate);
	}

	public io.intino.tara.magritte.Node createRoot(io.intino.tara.magritte.Concept concept, String namespace) {
		return graph.createRoot(concept, namespace);
	}

	public <T extends io.intino.tara.magritte.Layer> T createRoot(java.lang.Class<T> layerClass, String namespace) {
		return graph.createRoot(layerClass, namespace);
	}

	public io.intino.tara.magritte.Node createRoot(String concept, String namespace) {
		return graph.createRoot(concept, namespace);
	}

	public <T extends io.intino.tara.magritte.Layer> T createRoot(java.lang.Class<T> layerClass, String namespace, String id) {
		return graph.createRoot(layerClass, namespace, id);
	}

	public io.intino.tara.magritte.Node createRoot(String concept, String namespace, String id) {
		return graph.createRoot(concept, namespace, id);
	}

	public io.intino.tara.magritte.Node createRoot(io.intino.tara.magritte.Concept concept, String namespace, String id) {
		return graph.createRoot(concept, namespace, id);
	}

	public java.util.List<io.intino.ness.Function> functionList() {
		return functionList;
	}

	public java.util.List<io.intino.ness.Channel> channelList() {
		return channelList;
	}

	public java.util.List<io.intino.ness.Connection> connectionList() {
		return connectionList;
	}

	public java.util.List<io.intino.ness.User> userList() {
		return userList;
	}

	public java.util.List<io.intino.ness.Function> functionList(java.util.function.Predicate<io.intino.ness.Function> predicate) {
		return functionList.stream().filter(predicate).collect(java.util.stream.Collectors.toList());
	}

	public io.intino.ness.Function function(int index) {
		return functionList.get(index);
	}

	public java.util.List<io.intino.ness.Channel> channelList(java.util.function.Predicate<io.intino.ness.Channel> predicate) {
		return channelList.stream().filter(predicate).collect(java.util.stream.Collectors.toList());
	}

	public io.intino.ness.Channel channel(int index) {
		return channelList.get(index);
	}

	public java.util.List<io.intino.ness.Connection> connectionList(java.util.function.Predicate<io.intino.ness.Connection> predicate) {
		return connectionList.stream().filter(predicate).collect(java.util.stream.Collectors.toList());
	}

	public io.intino.ness.Connection connection(int index) {
		return connectionList.get(index);
	}

	public java.util.List<io.intino.ness.User> userList(java.util.function.Predicate<io.intino.ness.User> predicate) {
		return userList.stream().filter(predicate).collect(java.util.stream.Collectors.toList());
	}

	public io.intino.ness.User user(int index) {
		return userList.get(index);
	}

	public io.intino.tara.magritte.Graph graph() {
		return graph;
	}

	public Create create() {
		return new Create("Misc", null);
	}

	public Create create(String namespace) {
		return new Create(namespace, null);
	}

	public Create create(String namespace, String name) {
		return new Create(namespace, name);
	}

	public class Create {
		private final String namespace;
		private final String name;

		public Create(String namespace, String name) {
			this.namespace = namespace;
			this.name = name;
		}

		public io.intino.ness.Function function(java.lang.String source) {
			io.intino.ness.Function newElement = GraphWrapper.this.graph.createRoot(io.intino.ness.Function.class, namespace, name).as(io.intino.ness.Function.class);
			newElement.node().set(newElement, "source", java.util.Collections.singletonList(source));
			return newElement;
		}

		public io.intino.ness.Channel channel(java.lang.String qualifiedName) {
			io.intino.ness.Channel newElement = GraphWrapper.this.graph.createRoot(io.intino.ness.Channel.class, namespace, name).as(io.intino.ness.Channel.class);
			newElement.node().set(newElement, "qualifiedName", java.util.Collections.singletonList(qualifiedName));
			return newElement;
		}

		public io.intino.ness.Connection connection(io.intino.ness.Channel faucet, io.intino.ness.Channel flooder, io.intino.ness.Function plug) {
			io.intino.ness.Connection newElement = GraphWrapper.this.graph.createRoot(io.intino.ness.Connection.class, namespace, name).as(io.intino.ness.Connection.class);
			newElement.node().set(newElement, "faucet", java.util.Collections.singletonList(faucet));
			newElement.node().set(newElement, "flooder", java.util.Collections.singletonList(flooder));
			newElement.node().set(newElement, "plug", java.util.Collections.singletonList(plug));
			return newElement;
		}

		public io.intino.ness.User user(java.lang.String password, java.util.List<java.lang.String> groups) {
			io.intino.ness.User newElement = GraphWrapper.this.graph.createRoot(io.intino.ness.User.class, namespace, name).as(io.intino.ness.User.class);
			newElement.node().set(newElement, "password", java.util.Collections.singletonList(password));
			newElement.node().set(newElement, "groups", groups);
			return newElement;
		}

	}


}