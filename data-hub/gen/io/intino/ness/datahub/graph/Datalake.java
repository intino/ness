package io.intino.ness.datahub.graph;

import io.intino.ness.datahub.graph.*;

public class Datalake  extends io.intino.tara.magritte.Layer implements io.intino.tara.magritte.tags.Terminal {
	protected Scale scale;

	public enum Scale {
		Year, Month, Day, Hour, Minute;
	}
	protected java.lang.String path;
	protected java.util.List<io.intino.ness.datahub.graph.Datalake.Split> splitList = new java.util.ArrayList<>();
	protected java.util.List<io.intino.ness.datahub.graph.Datalake.Tank> tankList = new java.util.ArrayList<>();

	public Datalake(io.intino.tara.magritte.Node node) {
		super(node);
	}

	public Scale scale() {
		return scale;
	}

	public java.lang.String path() {
		return path;
	}

	public Datalake scale(io.intino.ness.datahub.graph.Datalake.Scale value) {
		this.scale = value;
		return (Datalake) this;
	}

	public Datalake path(java.lang.String value) {
		this.path = value;
		return (Datalake) this;
	}

	public java.util.List<io.intino.ness.datahub.graph.Datalake.Split> splitList() {
		return java.util.Collections.unmodifiableList(splitList);
	}

	public io.intino.ness.datahub.graph.Datalake.Split split(int index) {
		return splitList.get(index);
	}

	public java.util.List<io.intino.ness.datahub.graph.Datalake.Split> splitList(java.util.function.Predicate<io.intino.ness.datahub.graph.Datalake.Split> predicate) {
		return splitList().stream().filter(predicate).collect(java.util.stream.Collectors.toList());
	}

	public java.util.List<io.intino.ness.datahub.graph.Datalake.Tank> tankList() {
		return java.util.Collections.unmodifiableList(tankList);
	}

	public io.intino.ness.datahub.graph.Datalake.Tank tank(int index) {
		return tankList.get(index);
	}

	public java.util.List<io.intino.ness.datahub.graph.Datalake.Tank> tankList(java.util.function.Predicate<io.intino.ness.datahub.graph.Datalake.Tank> predicate) {
		return tankList().stream().filter(predicate).collect(java.util.stream.Collectors.toList());
	}

	protected java.util.List<io.intino.tara.magritte.Node> componentList$() {
		java.util.Set<io.intino.tara.magritte.Node> components = new java.util.LinkedHashSet<>(super.componentList$());
		new java.util.ArrayList<>(splitList).forEach(c -> components.add(c.core$()));
		new java.util.ArrayList<>(tankList).forEach(c -> components.add(c.core$()));
		return new java.util.ArrayList<>(components);
	}

	@Override
	protected java.util.Map<java.lang.String, java.util.List<?>> variables$() {
		java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>();
		map.put("scale", new java.util.ArrayList(java.util.Collections.singletonList(this.scale)));
		map.put("path", new java.util.ArrayList(java.util.Collections.singletonList(this.path)));
		return map;
	}

	@Override
	protected void addNode$(io.intino.tara.magritte.Node node) {
		super.addNode$(node);
		if (node.is("Datalake$Split")) this.splitList.add(node.as(io.intino.ness.datahub.graph.Datalake.Split.class));
		if (node.is("Datalake$Tank")) this.tankList.add(node.as(io.intino.ness.datahub.graph.Datalake.Tank.class));
	}

	@Override
	    protected void removeNode$(io.intino.tara.magritte.Node node) {
	        super.removeNode$(node);
	        if (node.is("Datalake$Split")) this.splitList.remove(node.as(io.intino.ness.datahub.graph.Datalake.Split.class));
	        if (node.is("Datalake$Tank")) this.tankList.remove(node.as(io.intino.ness.datahub.graph.Datalake.Tank.class));
	    }

	@Override
	protected void load$(java.lang.String name, java.util.List<?> values) {
		super.load$(name, values);
		if (name.equalsIgnoreCase("scale")) this.scale = io.intino.tara.magritte.loaders.WordLoader.load(values, Scale.class, this).get(0);
		else if (name.equalsIgnoreCase("path")) this.path = io.intino.tara.magritte.loaders.StringLoader.load(values, this).get(0);
	}

	@Override
	protected void set$(java.lang.String name, java.util.List<?> values) {
		super.set$(name, values);
		if (name.equalsIgnoreCase("scale")) this.scale = (Scale) values.get(0);
		else if (name.equalsIgnoreCase("path")) this.path = (java.lang.String) values.get(0);
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

		public io.intino.ness.datahub.graph.Datalake.Split split(java.util.List<java.lang.String> values) {
		    io.intino.ness.datahub.graph.Datalake.Split newElement = core$().graph().concept(io.intino.ness.datahub.graph.Datalake.Split.class).createNode(this.name, core$()).as(io.intino.ness.datahub.graph.Datalake.Split.class);
			newElement.core$().set(newElement, "values", values);
		    return newElement;
		}

		public io.intino.ness.datahub.graph.Datalake.Tank tank(java.lang.String name) {
		    io.intino.ness.datahub.graph.Datalake.Tank newElement = core$().graph().concept(io.intino.ness.datahub.graph.Datalake.Tank.class).createNode(this.name, core$()).as(io.intino.ness.datahub.graph.Datalake.Tank.class);
			newElement.core$().set(newElement, "name", java.util.Collections.singletonList(name));
		    return newElement;
		}

	}

	public Clear clear() {
		return new Clear();
	}

	public class Clear  {
		public void split(java.util.function.Predicate<io.intino.ness.datahub.graph.Datalake.Split> filter) {
			new java.util.ArrayList<>(splitList()).stream().filter(filter).forEach(io.intino.tara.magritte.Layer::delete$);
		}

		public void tank(java.util.function.Predicate<io.intino.ness.datahub.graph.Datalake.Tank> filter) {
			new java.util.ArrayList<>(tankList()).stream().filter(filter).forEach(io.intino.tara.magritte.Layer::delete$);
		}
	}

	public static class Split  extends io.intino.tara.magritte.Layer implements io.intino.tara.magritte.tags.Terminal {
		protected java.util.List<java.lang.String> values = new java.util.ArrayList<>();

		public Split(io.intino.tara.magritte.Node node) {
			super(node);
		}

		public java.util.List<java.lang.String> values() {
			return values;
		}

		public java.lang.String values(int index) {
			return values.get(index);
		}

		public java.util.List<java.lang.String> values(java.util.function.Predicate<java.lang.String> predicate) {
			return values().stream().filter(predicate).collect(java.util.stream.Collectors.toList());
		}

		@Override
		protected java.util.Map<java.lang.String, java.util.List<?>> variables$() {
			java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>();
			map.put("values", this.values);
			return map;
		}

		@Override
		protected void load$(java.lang.String name, java.util.List<?> values) {
			super.load$(name, values);
			if (name.equalsIgnoreCase("values")) this.values = io.intino.tara.magritte.loaders.StringLoader.load(values, this);
		}

		@Override
		protected void set$(java.lang.String name, java.util.List<?> values) {
			super.set$(name, values);
			if (name.equalsIgnoreCase("values")) this.values = new java.util.ArrayList<>((java.util.List<java.lang.String>) values);
		}

		public io.intino.ness.datahub.graph.NessGraph graph() {
			return (io.intino.ness.datahub.graph.NessGraph) core$().graph().as(io.intino.ness.datahub.graph.NessGraph.class);
		}
	}

	public static class Tank  extends io.intino.tara.magritte.Layer implements io.intino.tara.magritte.tags.Terminal {
		protected java.lang.String name;

		public Tank(io.intino.tara.magritte.Node node) {
			super(node);
		}

		public java.lang.String name() {
			return name;
		}

		public Tank name(java.lang.String value) {
			this.name = value;
			return (Tank) this;
		}

		public io.intino.ness.datahub.graph.set.datalake.SetTank asSet() {
			io.intino.tara.magritte.Layer as = a$(io.intino.ness.datahub.graph.set.datalake.SetTank.class);
			return as != null ? (io.intino.ness.datahub.graph.set.datalake.SetTank) as : core$().addFacet(io.intino.ness.datahub.graph.set.datalake.SetTank.class);
		}

		public boolean isSet() {
			return core$().is(io.intino.ness.datahub.graph.set.datalake.SetTank.class);
		}

		public io.intino.ness.datahub.graph.tanktype.datalake.TankTypeTank asTankType() {
			io.intino.tara.magritte.Layer as = a$(io.intino.ness.datahub.graph.tanktype.datalake.TankTypeTank.class);
			return as != null ? (io.intino.ness.datahub.graph.tanktype.datalake.TankTypeTank) as : null;
		}

		public boolean isTankType() {
			return core$().is(io.intino.ness.datahub.graph.tanktype.datalake.TankTypeTank.class);
		}

		public io.intino.ness.datahub.graph.event.datalake.EventTank asEvent() {
			io.intino.tara.magritte.Layer as = a$(io.intino.ness.datahub.graph.event.datalake.EventTank.class);
			return as != null ? (io.intino.ness.datahub.graph.event.datalake.EventTank) as : core$().addFacet(io.intino.ness.datahub.graph.event.datalake.EventTank.class);
		}

		public boolean isEvent() {
			return core$().is(io.intino.ness.datahub.graph.event.datalake.EventTank.class);
		}

		@Override
		protected java.util.Map<java.lang.String, java.util.List<?>> variables$() {
			java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>();
			map.put("name", new java.util.ArrayList(java.util.Collections.singletonList(this.name)));
			return map;
		}

		@Override
		protected void load$(java.lang.String name, java.util.List<?> values) {
			super.load$(name, values);
			if (name.equalsIgnoreCase("name")) this.name = io.intino.tara.magritte.loaders.StringLoader.load(values, this).get(0);
		}

		@Override
		protected void set$(java.lang.String name, java.util.List<?> values) {
			super.set$(name, values);
			if (name.equalsIgnoreCase("name")) this.name = (java.lang.String) values.get(0);
		}

		public io.intino.ness.datahub.graph.NessGraph graph() {
			return (io.intino.ness.datahub.graph.NessGraph) core$().graph().as(io.intino.ness.datahub.graph.NessGraph.class);
		}
	}


	public io.intino.ness.datahub.graph.NessGraph graph() {
		return (io.intino.ness.datahub.graph.NessGraph) core$().graph().as(io.intino.ness.datahub.graph.NessGraph.class);
	}
}