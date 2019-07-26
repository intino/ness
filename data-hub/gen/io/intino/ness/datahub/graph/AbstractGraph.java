package io.intino.ness.datahub.graph;

import io.intino.tara.magritte.Graph;

public class AbstractGraph extends io.intino.tara.magritte.GraphWrapper {

	protected io.intino.tara.magritte.Graph graph;
	private io.intino.ness.datahub.graph.Broker broker;
	private io.intino.ness.datahub.graph.Datalake datalake;
	private java.util.List<io.intino.ness.datahub.graph.Adapter> adapterList = new java.util.ArrayList<>();
	private java.util.List<io.intino.ness.datahub.graph.adaptertype.AdapterTypeAdapter> adapterTypeAdapterList = new java.util.ArrayList<>();
	private java.util.List<io.intino.ness.datahub.graph.realtime.RealtimeAdapter> realtimeAdapterList = new java.util.ArrayList<>();
	private java.util.List<io.intino.ness.datahub.graph.batch.BatchAdapter> batchAdapterList = new java.util.ArrayList<>();
	private java.util.List<io.intino.ness.datahub.graph.tanktype.TankTypeAdapter> tankTypeAdapterList = new java.util.ArrayList<>();
	private java.util.List<io.intino.ness.datahub.graph.set.SetAdapter> setAdapterList = new java.util.ArrayList<>();
	private java.util.List<io.intino.ness.datahub.graph.event.EventAdapter> eventAdapterList = new java.util.ArrayList<>();
	private java.util.List<io.intino.ness.datahub.graph.custom.CustomAdapter> customAdapterList = new java.util.ArrayList<>();

	private java.util.Map<String, Indexer> _index = _fillIndex();

	public AbstractGraph(io.intino.tara.magritte.Graph graph) {
		this.graph = graph;
		this.graph.i18n().register("Ness");
	}

	public AbstractGraph(io.intino.tara.magritte.Graph graph, AbstractGraph wrapper) {
		this.graph = graph;
		this.graph.i18n().register("Ness");
		this.broker = wrapper.broker;
		this.datalake = wrapper.datalake;
		this.adapterList = new java.util.ArrayList<>(wrapper.adapterList);
		this.adapterTypeAdapterList = new java.util.ArrayList<>(wrapper.adapterTypeAdapterList);
		this.realtimeAdapterList = new java.util.ArrayList<>(wrapper.realtimeAdapterList);
		this.batchAdapterList = new java.util.ArrayList<>(wrapper.batchAdapterList);
		this.tankTypeAdapterList = new java.util.ArrayList<>(wrapper.tankTypeAdapterList);
		this.setAdapterList = new java.util.ArrayList<>(wrapper.setAdapterList);
		this.eventAdapterList = new java.util.ArrayList<>(wrapper.eventAdapterList);
		this.customAdapterList = new java.util.ArrayList<>(wrapper.customAdapterList);
	}

	public <T extends io.intino.tara.magritte.GraphWrapper> T a$(Class<T> t) {
		return this.core$().as(t);
	}

    @Override
	public void update() {
		this._index.values().forEach(v -> v.clear());
		graph.rootList().forEach(r -> addNode$(r));
	}

	@Override
	protected void addNode$(io.intino.tara.magritte.Node node) {
		for (io.intino.tara.magritte.Concept c : node.conceptList()) if (this._index.containsKey(c.id())) this._index.get(c.id()).add(node);
		if (this._index.containsKey(node.id())) this._index.get(node.id()).add(node);
	}

	@Override
	protected void removeNode$(io.intino.tara.magritte.Node node) {
		for (io.intino.tara.magritte.Concept c : node.conceptList()) if (this._index.containsKey(c.id())) this._index.get(c.id()).remove(node);
		if (this._index.containsKey(node.id())) this._index.get(node.id()).remove(node);
	}

	public java.net.URL resourceAsMessage$(String language, String key) {
		return graph.loadResource(graph.i18n().message(language, key));
	}

	public io.intino.ness.datahub.graph.Broker broker() {
		return broker;
	}

	public io.intino.ness.datahub.graph.Datalake datalake() {
		return datalake;
	}

	public java.util.List<io.intino.ness.datahub.graph.Adapter> adapterList() {
		return adapterList;
	}

	public java.util.List<io.intino.ness.datahub.graph.adaptertype.AdapterTypeAdapter> adapterTypeAdapterList() {
		return adapterTypeAdapterList;
	}

	public java.util.List<io.intino.ness.datahub.graph.realtime.RealtimeAdapter> realtimeAdapterList() {
		return realtimeAdapterList;
	}

	public java.util.List<io.intino.ness.datahub.graph.batch.BatchAdapter> batchAdapterList() {
		return batchAdapterList;
	}

	public java.util.List<io.intino.ness.datahub.graph.tanktype.TankTypeAdapter> tankTypeAdapterList() {
		return tankTypeAdapterList;
	}

	public java.util.List<io.intino.ness.datahub.graph.set.SetAdapter> setAdapterList() {
		return setAdapterList;
	}

	public java.util.List<io.intino.ness.datahub.graph.event.EventAdapter> eventAdapterList() {
		return eventAdapterList;
	}

	public java.util.List<io.intino.ness.datahub.graph.custom.CustomAdapter> customAdapterList() {
		return customAdapterList;
	}

	public java.util.stream.Stream<io.intino.ness.datahub.graph.Adapter> adapterList(java.util.function.Predicate<io.intino.ness.datahub.graph.Adapter> filter) {
		return adapterList.stream().filter(filter);
	}

	public io.intino.ness.datahub.graph.Adapter adapter(int index) {
		return adapterList.get(index);
	}

	public java.util.stream.Stream<io.intino.ness.datahub.graph.adaptertype.AdapterTypeAdapter> adapterTypeAdapterList(java.util.function.Predicate<io.intino.ness.datahub.graph.adaptertype.AdapterTypeAdapter> filter) {
		return adapterTypeAdapterList.stream().filter(filter);
	}

	public io.intino.ness.datahub.graph.adaptertype.AdapterTypeAdapter adapterTypeAdapter(int index) {
		return adapterTypeAdapterList.get(index);
	}

	public java.util.stream.Stream<io.intino.ness.datahub.graph.realtime.RealtimeAdapter> realtimeAdapterList(java.util.function.Predicate<io.intino.ness.datahub.graph.realtime.RealtimeAdapter> filter) {
		return realtimeAdapterList.stream().filter(filter);
	}

	public io.intino.ness.datahub.graph.realtime.RealtimeAdapter realtimeAdapter(int index) {
		return realtimeAdapterList.get(index);
	}

	public java.util.stream.Stream<io.intino.ness.datahub.graph.batch.BatchAdapter> batchAdapterList(java.util.function.Predicate<io.intino.ness.datahub.graph.batch.BatchAdapter> filter) {
		return batchAdapterList.stream().filter(filter);
	}

	public io.intino.ness.datahub.graph.batch.BatchAdapter batchAdapter(int index) {
		return batchAdapterList.get(index);
	}

	public java.util.stream.Stream<io.intino.ness.datahub.graph.tanktype.TankTypeAdapter> tankTypeAdapterList(java.util.function.Predicate<io.intino.ness.datahub.graph.tanktype.TankTypeAdapter> filter) {
		return tankTypeAdapterList.stream().filter(filter);
	}

	public io.intino.ness.datahub.graph.tanktype.TankTypeAdapter tankTypeAdapter(int index) {
		return tankTypeAdapterList.get(index);
	}

	public java.util.stream.Stream<io.intino.ness.datahub.graph.set.SetAdapter> setAdapterList(java.util.function.Predicate<io.intino.ness.datahub.graph.set.SetAdapter> filter) {
		return setAdapterList.stream().filter(filter);
	}

	public io.intino.ness.datahub.graph.set.SetAdapter setAdapter(int index) {
		return setAdapterList.get(index);
	}

	public java.util.stream.Stream<io.intino.ness.datahub.graph.event.EventAdapter> eventAdapterList(java.util.function.Predicate<io.intino.ness.datahub.graph.event.EventAdapter> filter) {
		return eventAdapterList.stream().filter(filter);
	}

	public io.intino.ness.datahub.graph.event.EventAdapter eventAdapter(int index) {
		return eventAdapterList.get(index);
	}

	public java.util.stream.Stream<io.intino.ness.datahub.graph.custom.CustomAdapter> customAdapterList(java.util.function.Predicate<io.intino.ness.datahub.graph.custom.CustomAdapter> filter) {
		return customAdapterList.stream().filter(filter);
	}

	public io.intino.ness.datahub.graph.custom.CustomAdapter customAdapter(int index) {
		return customAdapterList.get(index);
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

		public io.intino.ness.datahub.graph.Broker broker(int port, int secondaryPort) {
			io.intino.ness.datahub.graph.Broker newElement = AbstractGraph.this.graph.createRoot(io.intino.ness.datahub.graph.Broker.class, stash, this.name).a$(io.intino.ness.datahub.graph.Broker.class);
			newElement.core$().set(newElement, "port", java.util.Collections.singletonList(port));
			newElement.core$().set(newElement, "secondaryPort", java.util.Collections.singletonList(secondaryPort));
			return newElement;
		}

		public io.intino.ness.datahub.graph.Datalake datalake() {
			io.intino.ness.datahub.graph.Datalake newElement = AbstractGraph.this.graph.createRoot(io.intino.ness.datahub.graph.Datalake.class, stash, this.name).a$(io.intino.ness.datahub.graph.Datalake.class);
			return newElement;
		}

		public io.intino.ness.datahub.graph.Adapter adapter() {
			io.intino.ness.datahub.graph.Adapter newElement = AbstractGraph.this.graph.createRoot(io.intino.ness.datahub.graph.Adapter.class, stash, this.name).a$(io.intino.ness.datahub.graph.Adapter.class);
			return newElement;
		}

		public io.intino.ness.datahub.graph.realtime.RealtimeAdapter realtimeAdapter() {
			io.intino.ness.datahub.graph.realtime.RealtimeAdapter newElement = AbstractGraph.this.graph.createRoot(io.intino.ness.datahub.graph.realtime.RealtimeAdapter.class, stash, this.name).a$(io.intino.ness.datahub.graph.realtime.RealtimeAdapter.class);
			return newElement;
		}

		public io.intino.ness.datahub.graph.batch.BatchAdapter batchAdapter() {
			io.intino.ness.datahub.graph.batch.BatchAdapter newElement = AbstractGraph.this.graph.createRoot(io.intino.ness.datahub.graph.batch.BatchAdapter.class, stash, this.name).a$(io.intino.ness.datahub.graph.batch.BatchAdapter.class);
			return newElement;
		}

		public io.intino.ness.datahub.graph.set.SetAdapter setAdapter(java.util.List<io.intino.ness.datahub.graph.Datalake.Tank> tanks, java.lang.String startTimetag, java.lang.String endTimetag, io.intino.ness.datahub.graph.functions.SetAdapter adapt) {
			io.intino.ness.datahub.graph.set.SetAdapter newElement = AbstractGraph.this.graph.createRoot(io.intino.ness.datahub.graph.set.SetAdapter.class, stash, this.name).a$(io.intino.ness.datahub.graph.set.SetAdapter.class);
			newElement.core$().set(newElement, "tanks", tanks);
			newElement.core$().set(newElement, "startTimetag", java.util.Collections.singletonList(startTimetag));
			newElement.core$().set(newElement, "endTimetag", java.util.Collections.singletonList(endTimetag));
			newElement.core$().set(newElement, "adapt", java.util.Collections.singletonList(adapt));
			return newElement;
		}

		public io.intino.ness.datahub.graph.event.EventAdapter eventAdapter(java.util.List<io.intino.ness.datahub.graph.Datalake.Tank> tanks, java.lang.String startTimetag, java.lang.String endTimetag, io.intino.ness.datahub.graph.functions.EventAdapter adapt) {
			io.intino.ness.datahub.graph.event.EventAdapter newElement = AbstractGraph.this.graph.createRoot(io.intino.ness.datahub.graph.event.EventAdapter.class, stash, this.name).a$(io.intino.ness.datahub.graph.event.EventAdapter.class);
			newElement.core$().set(newElement, "tanks", tanks);
			newElement.core$().set(newElement, "startTimetag", java.util.Collections.singletonList(startTimetag));
			newElement.core$().set(newElement, "endTimetag", java.util.Collections.singletonList(endTimetag));
			newElement.core$().set(newElement, "adapt", java.util.Collections.singletonList(adapt));
			return newElement;
		}

		public io.intino.ness.datahub.graph.custom.CustomAdapter customAdapter(io.intino.ness.datahub.graph.functions.DatalakeAdapter adapt) {
			io.intino.ness.datahub.graph.custom.CustomAdapter newElement = AbstractGraph.this.graph.createRoot(io.intino.ness.datahub.graph.custom.CustomAdapter.class, stash, this.name).a$(io.intino.ness.datahub.graph.custom.CustomAdapter.class);
			newElement.core$().set(newElement, "adapt", java.util.Collections.singletonList(adapt));
			return newElement;
		}
	}

	public class Clear {
	    public void adapter(java.util.function.Predicate<io.intino.ness.datahub.graph.Adapter> filter) {
	    	new java.util.ArrayList<>(AbstractGraph.this.adapterList()).stream().filter(filter).forEach(io.intino.tara.magritte.Layer::delete$);
	    }

	    public void realtimeAdapter(java.util.function.Predicate<io.intino.ness.datahub.graph.realtime.RealtimeAdapter> filter) {
	    	new java.util.ArrayList<>(AbstractGraph.this.realtimeAdapterList()).stream().filter(filter).forEach(io.intino.tara.magritte.Layer::delete$);
	    }

	    public void batchAdapter(java.util.function.Predicate<io.intino.ness.datahub.graph.batch.BatchAdapter> filter) {
	    	new java.util.ArrayList<>(AbstractGraph.this.batchAdapterList()).stream().filter(filter).forEach(io.intino.tara.magritte.Layer::delete$);
	    }

	    public void setAdapter(java.util.function.Predicate<io.intino.ness.datahub.graph.set.SetAdapter> filter) {
	    	new java.util.ArrayList<>(AbstractGraph.this.setAdapterList()).stream().filter(filter).forEach(io.intino.tara.magritte.Layer::delete$);
	    }

	    public void eventAdapter(java.util.function.Predicate<io.intino.ness.datahub.graph.event.EventAdapter> filter) {
	    	new java.util.ArrayList<>(AbstractGraph.this.eventAdapterList()).stream().filter(filter).forEach(io.intino.tara.magritte.Layer::delete$);
	    }

	    public void customAdapter(java.util.function.Predicate<io.intino.ness.datahub.graph.custom.CustomAdapter> filter) {
	    	new java.util.ArrayList<>(AbstractGraph.this.customAdapterList()).stream().filter(filter).forEach(io.intino.tara.magritte.Layer::delete$);
	    }
	}


	private java.util.HashMap<String, Indexer> _fillIndex() {
		java.util.HashMap<String, Indexer> map = new java.util.HashMap<>();
		map.put("Broker", new Indexer(node -> broker = node.as(io.intino.ness.datahub.graph.Broker.class), node -> broker = null, () -> broker = null));
		map.put("Datalake", new Indexer(node -> datalake = node.as(io.intino.ness.datahub.graph.Datalake.class), node -> datalake = null, () -> datalake = null));
		map.put("Adapter", new Indexer(node -> adapterList.add(node.as(io.intino.ness.datahub.graph.Adapter.class)), node -> adapterList.remove(node.as(io.intino.ness.datahub.graph.Adapter.class)), () -> adapterList.clear()));
		map.put("AdapterType#Adapter", new Indexer(node -> adapterTypeAdapterList.add(node.as(io.intino.ness.datahub.graph.adaptertype.AdapterTypeAdapter.class)), node -> adapterTypeAdapterList.remove(node.as(io.intino.ness.datahub.graph.adaptertype.AdapterTypeAdapter.class)), () -> adapterTypeAdapterList.clear()));
		map.put("Realtime#Adapter", new Indexer(node -> realtimeAdapterList.add(node.as(io.intino.ness.datahub.graph.realtime.RealtimeAdapter.class)), node -> realtimeAdapterList.remove(node.as(io.intino.ness.datahub.graph.realtime.RealtimeAdapter.class)), () -> realtimeAdapterList.clear()));
		map.put("Batch#Adapter", new Indexer(node -> batchAdapterList.add(node.as(io.intino.ness.datahub.graph.batch.BatchAdapter.class)), node -> batchAdapterList.remove(node.as(io.intino.ness.datahub.graph.batch.BatchAdapter.class)), () -> batchAdapterList.clear()));
		map.put("TankType#Adapter", new Indexer(node -> tankTypeAdapterList.add(node.as(io.intino.ness.datahub.graph.tanktype.TankTypeAdapter.class)), node -> tankTypeAdapterList.remove(node.as(io.intino.ness.datahub.graph.tanktype.TankTypeAdapter.class)), () -> tankTypeAdapterList.clear()));
		map.put("Set#Adapter", new Indexer(node -> setAdapterList.add(node.as(io.intino.ness.datahub.graph.set.SetAdapter.class)), node -> setAdapterList.remove(node.as(io.intino.ness.datahub.graph.set.SetAdapter.class)), () -> setAdapterList.clear()));
		map.put("Event#Adapter", new Indexer(node -> eventAdapterList.add(node.as(io.intino.ness.datahub.graph.event.EventAdapter.class)), node -> eventAdapterList.remove(node.as(io.intino.ness.datahub.graph.event.EventAdapter.class)), () -> eventAdapterList.clear()));
		map.put("Custom#Adapter", new Indexer(node -> customAdapterList.add(node.as(io.intino.ness.datahub.graph.custom.CustomAdapter.class)), node -> customAdapterList.remove(node.as(io.intino.ness.datahub.graph.custom.CustomAdapter.class)), () -> customAdapterList.clear()));
		return map;
	}

	public static class Indexer {
		Add add;
		Remove remove;
		IndexClear clear;

		public Indexer(Add add, Remove remove, IndexClear clear) {
			this.add = add;
			this.remove = remove;
			this.clear = clear;
		}

		void add(io.intino.tara.magritte.Node node) {
			this.add.add(node);
		}

		void remove(io.intino.tara.magritte.Node node) {
			this.remove.remove(node);
		}

		void clear() {
			this.clear.clear();
		}
	}

	interface Add {
		void add(io.intino.tara.magritte.Node node);
	}

	interface Remove {
		void remove(io.intino.tara.magritte.Node node);
	}

	interface IndexClear {
		void clear();
	}
}