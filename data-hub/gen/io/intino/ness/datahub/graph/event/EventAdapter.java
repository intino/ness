package io.intino.ness.datahub.graph.event;

import io.intino.ness.datahub.graph.*;
import io.intino.alexandria.zim.ZimStream;
import io.intino.ness.datahub.datalake.adapter.Context;

public class EventAdapter extends io.intino.ness.datahub.graph.tanktype.TankTypeAdapter implements io.intino.tara.magritte.tags.Terminal {
	protected java.util.List<io.intino.ness.datahub.graph.Datalake.Tank> tanks = new java.util.ArrayList<>();
	protected java.lang.String startTimetag;
	protected java.lang.String endTimetag;
	protected io.intino.ness.datahub.graph.functions.EventAdapter adapt;

	public EventAdapter(io.intino.tara.magritte.Node node) {
		super(node);
	}

	public java.util.List<io.intino.ness.datahub.graph.Datalake.Tank> tanks() {
		return tanks;
	}

	public io.intino.ness.datahub.graph.Datalake.Tank tanks(int index) {
		return tanks.get(index);
	}

	public java.util.List<io.intino.ness.datahub.graph.Datalake.Tank> tanks(java.util.function.Predicate<io.intino.ness.datahub.graph.Datalake.Tank> predicate) {
		return tanks().stream().filter(predicate).collect(java.util.stream.Collectors.toList());
	}

	public java.lang.String startTimetag() {
		return startTimetag;
	}

	public java.lang.String endTimetag() {
		return endTimetag;
	}

	public void adapt(ZimStream stream, Context context) {
		 adapt.adapt(stream, context);
	}

	public EventAdapter startTimetag(java.lang.String value) {
		this.startTimetag = value;
		return (EventAdapter) this;
	}

	public EventAdapter endTimetag(java.lang.String value) {
		this.endTimetag = value;
		return (EventAdapter) this;
	}

	public EventAdapter adapt(io.intino.ness.datahub.graph.functions.EventAdapter value) {
		this.adapt = io.intino.tara.magritte.loaders.FunctionLoader.load(adapt, this, io.intino.ness.datahub.graph.functions.EventAdapter.class);
		return (EventAdapter) this;
	}

	@Override
	protected java.util.Map<java.lang.String, java.util.List<?>> variables$() {
		java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>(super.variables$());
		map.put("tanks", this.tanks);
		map.put("startTimetag", new java.util.ArrayList(java.util.Collections.singletonList(this.startTimetag)));
		map.put("endTimetag", new java.util.ArrayList(java.util.Collections.singletonList(this.endTimetag)));
		map.put("adapt", this.adapt != null ? new java.util.ArrayList(java.util.Collections.singletonList(this.adapt)) : java.util.Collections.emptyList());
		return map;
	}

	@Override
	protected void load$(java.lang.String name, java.util.List<?> values) {
		super.load$(name, values);
		_adapter.core$().load(_adapter, name, values);
		if (name.equalsIgnoreCase("tanks")) this.tanks = io.intino.tara.magritte.loaders.NodeLoader.load(values,  io.intino.ness.datahub.graph.Datalake.Tank.class, this);
		else if (name.equalsIgnoreCase("startTimetag")) this.startTimetag = io.intino.tara.magritte.loaders.StringLoader.load(values, this).get(0);
		else if (name.equalsIgnoreCase("endTimetag")) this.endTimetag = io.intino.tara.magritte.loaders.StringLoader.load(values, this).get(0);
		else if (name.equalsIgnoreCase("adapt")) this.adapt = io.intino.tara.magritte.loaders.FunctionLoader.load(values, this, io.intino.ness.datahub.graph.functions.EventAdapter.class).get(0);
	}

	@Override
	protected void set$(java.lang.String name, java.util.List<?> values) {
		super.set$(name, values);
		_adapter.core$().set(_adapter, name, values);
		if (name.equalsIgnoreCase("tanks")) this.tanks = ((java.util.List<java.lang.Object>) values).stream().
			map(s -> graph().core$().load(((io.intino.tara.magritte.Layer) s).core$().id()).as(io.intino.ness.datahub.graph.Datalake.Tank.class)).collect(java.util.stream.Collectors.toList());
		else if (name.equalsIgnoreCase("startTimetag")) this.startTimetag = (java.lang.String) values.get(0);
		else if (name.equalsIgnoreCase("endTimetag")) this.endTimetag = (java.lang.String) values.get(0);
		else if (name.equalsIgnoreCase("adapt")) this.adapt = io.intino.tara.magritte.loaders.FunctionLoader.load(values.get(0), this, io.intino.ness.datahub.graph.functions.EventAdapter.class);
	}

	public io.intino.ness.datahub.graph.NessGraph graph() {
		return (io.intino.ness.datahub.graph.NessGraph) core$().graph().as(io.intino.ness.datahub.graph.NessGraph.class);
	}
}