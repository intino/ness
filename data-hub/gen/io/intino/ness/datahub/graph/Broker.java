package io.intino.ness.datahub.graph;

import io.intino.ness.datahub.graph.*;
import io.intino.ness.datahub.broker.BrokerService;

public class Broker  extends io.intino.tara.magritte.Layer implements io.intino.tara.magritte.tags.Terminal {
	protected int port;
	protected int secondaryPort;
	protected io.intino.ness.datahub.graph.functions.BrokerImplementation implementation;
	protected java.util.List<io.intino.ness.datahub.graph.Broker.User> userList = new java.util.ArrayList<>();
	protected java.util.List<io.intino.ness.datahub.graph.Broker.Pipe> pipeList = new java.util.ArrayList<>();
	protected java.util.List<io.intino.ness.datahub.graph.Broker.Bridge> bridgeList = new java.util.ArrayList<>();

	public Broker(io.intino.tara.magritte.Node node) {
		super(node);
	}

	public int port() {
		return port;
	}

	public int secondaryPort() {
		return secondaryPort;
	}

	public BrokerService implementation() {
		return implementation.get();
	}

	public Broker port(int value) {
		this.port = value;
		return (Broker) this;
	}

	public Broker secondaryPort(int value) {
		this.secondaryPort = value;
		return (Broker) this;
	}

	public Broker implementation(io.intino.ness.datahub.graph.functions.BrokerImplementation value) {
		this.implementation = io.intino.tara.magritte.loaders.FunctionLoader.load(implementation, this, io.intino.ness.datahub.graph.functions.BrokerImplementation.class);
		return (Broker) this;
	}

	public java.util.List<io.intino.ness.datahub.graph.Broker.User> userList() {
		return java.util.Collections.unmodifiableList(userList);
	}

	public io.intino.ness.datahub.graph.Broker.User user(int index) {
		return userList.get(index);
	}

	public java.util.List<io.intino.ness.datahub.graph.Broker.User> userList(java.util.function.Predicate<io.intino.ness.datahub.graph.Broker.User> predicate) {
		return userList().stream().filter(predicate).collect(java.util.stream.Collectors.toList());
	}

	public java.util.List<io.intino.ness.datahub.graph.Broker.Pipe> pipeList() {
		return java.util.Collections.unmodifiableList(pipeList);
	}

	public io.intino.ness.datahub.graph.Broker.Pipe pipe(int index) {
		return pipeList.get(index);
	}

	public java.util.List<io.intino.ness.datahub.graph.Broker.Pipe> pipeList(java.util.function.Predicate<io.intino.ness.datahub.graph.Broker.Pipe> predicate) {
		return pipeList().stream().filter(predicate).collect(java.util.stream.Collectors.toList());
	}

	public java.util.List<io.intino.ness.datahub.graph.Broker.Bridge> bridgeList() {
		return java.util.Collections.unmodifiableList(bridgeList);
	}

	public io.intino.ness.datahub.graph.Broker.Bridge bridge(int index) {
		return bridgeList.get(index);
	}

	public java.util.List<io.intino.ness.datahub.graph.Broker.Bridge> bridgeList(java.util.function.Predicate<io.intino.ness.datahub.graph.Broker.Bridge> predicate) {
		return bridgeList().stream().filter(predicate).collect(java.util.stream.Collectors.toList());
	}

	protected java.util.List<io.intino.tara.magritte.Node> componentList$() {
		java.util.Set<io.intino.tara.magritte.Node> components = new java.util.LinkedHashSet<>(super.componentList$());
		new java.util.ArrayList<>(userList).forEach(c -> components.add(c.core$()));
		new java.util.ArrayList<>(pipeList).forEach(c -> components.add(c.core$()));
		new java.util.ArrayList<>(bridgeList).forEach(c -> components.add(c.core$()));
		return new java.util.ArrayList<>(components);
	}

	@Override
	protected java.util.Map<java.lang.String, java.util.List<?>> variables$() {
		java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>();
		map.put("port", new java.util.ArrayList(java.util.Collections.singletonList(this.port)));
		map.put("secondaryPort", new java.util.ArrayList(java.util.Collections.singletonList(this.secondaryPort)));
		map.put("implementation", this.implementation != null ? new java.util.ArrayList(java.util.Collections.singletonList(this.implementation)) : java.util.Collections.emptyList());
		return map;
	}

	@Override
	protected void addNode$(io.intino.tara.magritte.Node node) {
		super.addNode$(node);
		if (node.is("Broker$User")) this.userList.add(node.as(io.intino.ness.datahub.graph.Broker.User.class));
		if (node.is("Broker$Pipe")) this.pipeList.add(node.as(io.intino.ness.datahub.graph.Broker.Pipe.class));
		if (node.is("Broker$Bridge")) this.bridgeList.add(node.as(io.intino.ness.datahub.graph.Broker.Bridge.class));
	}

	@Override
	    protected void removeNode$(io.intino.tara.magritte.Node node) {
	        super.removeNode$(node);
	        if (node.is("Broker$User")) this.userList.remove(node.as(io.intino.ness.datahub.graph.Broker.User.class));
	        if (node.is("Broker$Pipe")) this.pipeList.remove(node.as(io.intino.ness.datahub.graph.Broker.Pipe.class));
	        if (node.is("Broker$Bridge")) this.bridgeList.remove(node.as(io.intino.ness.datahub.graph.Broker.Bridge.class));
	    }

	@Override
	protected void load$(java.lang.String name, java.util.List<?> values) {
		super.load$(name, values);
		if (name.equalsIgnoreCase("port")) this.port = io.intino.tara.magritte.loaders.IntegerLoader.load(values, this).get(0);
		else if (name.equalsIgnoreCase("secondaryPort")) this.secondaryPort = io.intino.tara.magritte.loaders.IntegerLoader.load(values, this).get(0);
		else if (name.equalsIgnoreCase("implementation")) this.implementation = io.intino.tara.magritte.loaders.FunctionLoader.load(values, this, io.intino.ness.datahub.graph.functions.BrokerImplementation.class).get(0);
	}

	@Override
	protected void set$(java.lang.String name, java.util.List<?> values) {
		super.set$(name, values);
		if (name.equalsIgnoreCase("port")) this.port = (java.lang.Integer) values.get(0);
		else if (name.equalsIgnoreCase("secondaryPort")) this.secondaryPort = (java.lang.Integer) values.get(0);
		else if (name.equalsIgnoreCase("implementation")) this.implementation = io.intino.tara.magritte.loaders.FunctionLoader.load(values.get(0), this, io.intino.ness.datahub.graph.functions.BrokerImplementation.class);
	}

	public Create create() {
		return new Create(null);
	}

	public Create create(java.lang.String name) {
		return new Create(name);
	}

	public class Create  {
		protected final java.lang.String name;

		public Create(java.lang.String name) {
			this.name = name;
		}

		public io.intino.ness.datahub.graph.Broker.User user(java.lang.String name, java.lang.String password) {
		    io.intino.ness.datahub.graph.Broker.User newElement = core$().graph().concept(io.intino.ness.datahub.graph.Broker.User.class).createNode(this.name, core$()).as(io.intino.ness.datahub.graph.Broker.User.class);
			newElement.core$().set(newElement, "name", java.util.Collections.singletonList(name));
			newElement.core$().set(newElement, "password", java.util.Collections.singletonList(password));
		    return newElement;
		}

		public io.intino.ness.datahub.graph.Broker.Pipe pipe(java.lang.String origin, java.lang.String destination) {
		    io.intino.ness.datahub.graph.Broker.Pipe newElement = core$().graph().concept(io.intino.ness.datahub.graph.Broker.Pipe.class).createNode(this.name, core$()).as(io.intino.ness.datahub.graph.Broker.Pipe.class);
			newElement.core$().set(newElement, "origin", java.util.Collections.singletonList(origin));
			newElement.core$().set(newElement, "destination", java.util.Collections.singletonList(destination));
		    return newElement;
		}

		public io.intino.ness.datahub.graph.Broker.Bridge bridge(io.intino.ness.datahub.graph.Broker.Bridge.Direction direction, java.util.List<java.lang.String> topics) {
		    io.intino.ness.datahub.graph.Broker.Bridge newElement = core$().graph().concept(io.intino.ness.datahub.graph.Broker.Bridge.class).createNode(this.name, core$()).as(io.intino.ness.datahub.graph.Broker.Bridge.class);
			newElement.core$().set(newElement, "direction", java.util.Collections.singletonList(direction));
			newElement.core$().set(newElement, "topics", topics);
		    return newElement;
		}

	}

	public Clear clear() {
		return new Clear();
	}

	public class Clear  {
		public void user(java.util.function.Predicate<io.intino.ness.datahub.graph.Broker.User> filter) {
			new java.util.ArrayList<>(userList()).stream().filter(filter).forEach(io.intino.tara.magritte.Layer::delete$);
		}

		public void pipe(java.util.function.Predicate<io.intino.ness.datahub.graph.Broker.Pipe> filter) {
			new java.util.ArrayList<>(pipeList()).stream().filter(filter).forEach(io.intino.tara.magritte.Layer::delete$);
		}

		public void bridge(java.util.function.Predicate<io.intino.ness.datahub.graph.Broker.Bridge> filter) {
			new java.util.ArrayList<>(bridgeList()).stream().filter(filter).forEach(io.intino.tara.magritte.Layer::delete$);
		}
	}

	public static class User  extends io.intino.tara.magritte.Layer implements io.intino.tara.magritte.tags.Terminal {
		protected java.lang.String name;
		protected java.lang.String password;

		public User(io.intino.tara.magritte.Node node) {
			super(node);
		}

		public java.lang.String name() {
			return name;
		}

		public java.lang.String password() {
			return password;
		}

		public User name(java.lang.String value) {
			this.name = value;
			return (User) this;
		}

		public User password(java.lang.String value) {
			this.password = value;
			return (User) this;
		}

		@Override
		protected java.util.Map<java.lang.String, java.util.List<?>> variables$() {
			java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>();
			map.put("name", new java.util.ArrayList(java.util.Collections.singletonList(this.name)));
			map.put("password", new java.util.ArrayList(java.util.Collections.singletonList(this.password)));
			return map;
		}

		@Override
		protected void load$(java.lang.String name, java.util.List<?> values) {
			super.load$(name, values);
			if (name.equalsIgnoreCase("name")) this.name = io.intino.tara.magritte.loaders.StringLoader.load(values, this).get(0);
			else if (name.equalsIgnoreCase("password")) this.password = io.intino.tara.magritte.loaders.StringLoader.load(values, this).get(0);
		}

		@Override
		protected void set$(java.lang.String name, java.util.List<?> values) {
			super.set$(name, values);
			if (name.equalsIgnoreCase("name")) this.name = (java.lang.String) values.get(0);
			else if (name.equalsIgnoreCase("password")) this.password = (java.lang.String) values.get(0);
		}

		public io.intino.ness.datahub.graph.NessGraph graph() {
			return (io.intino.ness.datahub.graph.NessGraph) core$().graph().as(io.intino.ness.datahub.graph.NessGraph.class);
		}
	}

	public static class Pipe  extends io.intino.tara.magritte.Layer implements io.intino.tara.magritte.tags.Terminal {
		protected java.lang.String origin;
		protected java.lang.String destination;

		public Pipe(io.intino.tara.magritte.Node node) {
			super(node);
		}

		public java.lang.String origin() {
			return origin;
		}

		public java.lang.String destination() {
			return destination;
		}

		public Pipe origin(java.lang.String value) {
			this.origin = value;
			return (Pipe) this;
		}

		public Pipe destination(java.lang.String value) {
			this.destination = value;
			return (Pipe) this;
		}

		@Override
		protected java.util.Map<java.lang.String, java.util.List<?>> variables$() {
			java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>();
			map.put("origin", new java.util.ArrayList(java.util.Collections.singletonList(this.origin)));
			map.put("destination", new java.util.ArrayList(java.util.Collections.singletonList(this.destination)));
			return map;
		}

		@Override
		protected void load$(java.lang.String name, java.util.List<?> values) {
			super.load$(name, values);
			if (name.equalsIgnoreCase("origin")) this.origin = io.intino.tara.magritte.loaders.StringLoader.load(values, this).get(0);
			else if (name.equalsIgnoreCase("destination")) this.destination = io.intino.tara.magritte.loaders.StringLoader.load(values, this).get(0);
		}

		@Override
		protected void set$(java.lang.String name, java.util.List<?> values) {
			super.set$(name, values);
			if (name.equalsIgnoreCase("origin")) this.origin = (java.lang.String) values.get(0);
			else if (name.equalsIgnoreCase("destination")) this.destination = (java.lang.String) values.get(0);
		}

		public io.intino.ness.datahub.graph.NessGraph graph() {
			return (io.intino.ness.datahub.graph.NessGraph) core$().graph().as(io.intino.ness.datahub.graph.NessGraph.class);
		}
	}

	public static class Bridge  extends io.intino.tara.magritte.Layer implements io.intino.tara.magritte.tags.Terminal {
		protected Direction direction;

		public enum Direction {
			outgoing, incoming;
		}
		protected java.util.List<java.lang.String> topics = new java.util.ArrayList<>();
		protected io.intino.ness.datahub.graph.Broker.Bridge.ExternalBus externalBus;

		public Bridge(io.intino.tara.magritte.Node node) {
			super(node);
		}

		public Direction direction() {
			return direction;
		}

		public java.util.List<java.lang.String> topics() {
			return topics;
		}

		public java.lang.String topics(int index) {
			return topics.get(index);
		}

		public java.util.List<java.lang.String> topics(java.util.function.Predicate<java.lang.String> predicate) {
			return topics().stream().filter(predicate).collect(java.util.stream.Collectors.toList());
		}

		public Bridge direction(io.intino.ness.datahub.graph.Broker.Bridge.Direction value) {
			this.direction = value;
			return (Bridge) this;
		}

		public io.intino.ness.datahub.graph.Broker.Bridge.ExternalBus externalBus() {
			return externalBus;
		}

		protected java.util.List<io.intino.tara.magritte.Node> componentList$() {
			java.util.Set<io.intino.tara.magritte.Node> components = new java.util.LinkedHashSet<>(super.componentList$());
			if (externalBus != null) components.add(this.externalBus.core$());
			return new java.util.ArrayList<>(components);
		}

		@Override
		protected java.util.Map<java.lang.String, java.util.List<?>> variables$() {
			java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>();
			map.put("direction", new java.util.ArrayList(java.util.Collections.singletonList(this.direction)));
			map.put("topics", this.topics);
			return map;
		}

		@Override
		protected void addNode$(io.intino.tara.magritte.Node node) {
			super.addNode$(node);
			if (node.is("Broker$Bridge$ExternalBus")) this.externalBus = node.as(io.intino.ness.datahub.graph.Broker.Bridge.ExternalBus.class);
		}

		@Override
		    protected void removeNode$(io.intino.tara.magritte.Node node) {
		        super.removeNode$(node);
		        if (node.is("Broker$Bridge$ExternalBus")) this.externalBus = null;
		    }

		@Override
		protected void load$(java.lang.String name, java.util.List<?> values) {
			super.load$(name, values);
			if (name.equalsIgnoreCase("direction")) this.direction = io.intino.tara.magritte.loaders.WordLoader.load(values, Direction.class, this).get(0);
			else if (name.equalsIgnoreCase("topics")) this.topics = io.intino.tara.magritte.loaders.StringLoader.load(values, this);
		}

		@Override
		protected void set$(java.lang.String name, java.util.List<?> values) {
			super.set$(name, values);
			if (name.equalsIgnoreCase("direction")) this.direction = (Direction) values.get(0);
			else if (name.equalsIgnoreCase("topics")) this.topics = new java.util.ArrayList<>((java.util.List<java.lang.String>) values);
		}

		public Create create() {
			return new Create(null);
		}

		public Create create(java.lang.String name) {
			return new Create(name);
		}

		public class Create  {
			protected final java.lang.String name;

			public Create(java.lang.String name) {
				this.name = name;
			}

			public io.intino.ness.datahub.graph.Broker.Bridge.ExternalBus externalBus(java.lang.String url, java.lang.String user, java.lang.String password, java.lang.String sessionId) {
			    io.intino.ness.datahub.graph.Broker.Bridge.ExternalBus newElement = core$().graph().concept(io.intino.ness.datahub.graph.Broker.Bridge.ExternalBus.class).createNode(this.name, core$()).as(io.intino.ness.datahub.graph.Broker.Bridge.ExternalBus.class);
				newElement.core$().set(newElement, "url", java.util.Collections.singletonList(url));
				newElement.core$().set(newElement, "user", java.util.Collections.singletonList(user));
				newElement.core$().set(newElement, "password", java.util.Collections.singletonList(password));
				newElement.core$().set(newElement, "sessionId", java.util.Collections.singletonList(sessionId));
			    return newElement;
			}

		}

		public static class ExternalBus  extends io.intino.tara.magritte.Layer implements io.intino.tara.magritte.tags.Terminal {
			protected java.lang.String url;
			protected java.lang.String user;
			protected java.lang.String password;
			protected java.lang.String sessionId;

			public ExternalBus(io.intino.tara.magritte.Node node) {
				super(node);
			}

			public java.lang.String url() {
				return url;
			}

			public java.lang.String user() {
				return user;
			}

			public java.lang.String password() {
				return password;
			}

			public java.lang.String sessionId() {
				return sessionId;
			}

			public ExternalBus url(java.lang.String value) {
				this.url = value;
				return (ExternalBus) this;
			}

			public ExternalBus user(java.lang.String value) {
				this.user = value;
				return (ExternalBus) this;
			}

			public ExternalBus password(java.lang.String value) {
				this.password = value;
				return (ExternalBus) this;
			}

			public ExternalBus sessionId(java.lang.String value) {
				this.sessionId = value;
				return (ExternalBus) this;
			}

			@Override
			protected java.util.Map<java.lang.String, java.util.List<?>> variables$() {
				java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>();
				map.put("url", new java.util.ArrayList(java.util.Collections.singletonList(this.url)));
				map.put("user", new java.util.ArrayList(java.util.Collections.singletonList(this.user)));
				map.put("password", new java.util.ArrayList(java.util.Collections.singletonList(this.password)));
				map.put("sessionId", new java.util.ArrayList(java.util.Collections.singletonList(this.sessionId)));
				return map;
			}

			@Override
			protected void load$(java.lang.String name, java.util.List<?> values) {
				super.load$(name, values);
				if (name.equalsIgnoreCase("url")) this.url = io.intino.tara.magritte.loaders.StringLoader.load(values, this).get(0);
				else if (name.equalsIgnoreCase("user")) this.user = io.intino.tara.magritte.loaders.StringLoader.load(values, this).get(0);
				else if (name.equalsIgnoreCase("password")) this.password = io.intino.tara.magritte.loaders.StringLoader.load(values, this).get(0);
				else if (name.equalsIgnoreCase("sessionId")) this.sessionId = io.intino.tara.magritte.loaders.StringLoader.load(values, this).get(0);
			}

			@Override
			protected void set$(java.lang.String name, java.util.List<?> values) {
				super.set$(name, values);
				if (name.equalsIgnoreCase("url")) this.url = (java.lang.String) values.get(0);
				else if (name.equalsIgnoreCase("user")) this.user = (java.lang.String) values.get(0);
				else if (name.equalsIgnoreCase("password")) this.password = (java.lang.String) values.get(0);
				else if (name.equalsIgnoreCase("sessionId")) this.sessionId = (java.lang.String) values.get(0);
			}

			public io.intino.ness.datahub.graph.NessGraph graph() {
				return (io.intino.ness.datahub.graph.NessGraph) core$().graph().as(io.intino.ness.datahub.graph.NessGraph.class);
			}
		}


		public io.intino.ness.datahub.graph.NessGraph graph() {
			return (io.intino.ness.datahub.graph.NessGraph) core$().graph().as(io.intino.ness.datahub.graph.NessGraph.class);
		}
	}


	public io.intino.ness.datahub.graph.NessGraph graph() {
		return (io.intino.ness.datahub.graph.NessGraph) core$().graph().as(io.intino.ness.datahub.graph.NessGraph.class);
	}
}