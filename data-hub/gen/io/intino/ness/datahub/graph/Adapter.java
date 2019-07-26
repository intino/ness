package io.intino.ness.datahub.graph;

import io.intino.ness.datahub.graph.*;
import io.intino.alexandria.datalake.Datalake;
import io.intino.alexandria.zet.ZetStream;
import io.intino.alexandria.zim.ZimStream;
import io.intino.ness.datahub.datalake.adapter.Context;

public class Adapter  extends io.intino.tara.magritte.Layer implements io.intino.tara.magritte.tags.Terminal {

	public Adapter(io.intino.tara.magritte.Node node) {
		super(node);
	}

	public io.intino.ness.datahub.graph.set.SetAdapter asSet() {
		return a$(io.intino.ness.datahub.graph.set.SetAdapter.class);
	}

	public io.intino.ness.datahub.graph.set.SetAdapter asSet(java.util.List<io.intino.ness.datahub.graph.Datalake.Tank> tanks, java.lang.String startTimetag, java.lang.String endTimetag, io.intino.ness.datahub.graph.functions.SetAdapter adapt) {
		io.intino.ness.datahub.graph.set.SetAdapter newElement = core$().addFacet(io.intino.ness.datahub.graph.set.SetAdapter.class);
		newElement.core$().set(newElement, "tanks", tanks);
		newElement.core$().set(newElement, "startTimetag", java.util.Collections.singletonList(startTimetag));
		newElement.core$().set(newElement, "endTimetag", java.util.Collections.singletonList(endTimetag));
		newElement.core$().set(newElement, "adapt", java.util.Collections.singletonList(adapt));
	    return newElement;
	}

	public boolean isSet() {
		return core$().is(io.intino.ness.datahub.graph.set.SetAdapter.class);
	}

	public void removeSet() {
		core$().removeFacet(io.intino.ness.datahub.graph.set.SetAdapter.class);
	}

	public io.intino.ness.datahub.graph.tanktype.TankTypeAdapter asTankType() {
		io.intino.tara.magritte.Layer as = a$(io.intino.ness.datahub.graph.tanktype.TankTypeAdapter.class);
		return as != null ? (io.intino.ness.datahub.graph.tanktype.TankTypeAdapter) as : null;
	}

	public boolean isTankType() {
		return core$().is(io.intino.ness.datahub.graph.tanktype.TankTypeAdapter.class);
	}

	public io.intino.ness.datahub.graph.batch.BatchAdapter asBatch() {
		io.intino.tara.magritte.Layer as = a$(io.intino.ness.datahub.graph.batch.BatchAdapter.class);
		return as != null ? (io.intino.ness.datahub.graph.batch.BatchAdapter) as : core$().addFacet(io.intino.ness.datahub.graph.batch.BatchAdapter.class);
	}

	public boolean isBatch() {
		return core$().is(io.intino.ness.datahub.graph.batch.BatchAdapter.class);
	}

	public io.intino.ness.datahub.graph.adaptertype.AdapterTypeAdapter asAdapterType() {
		io.intino.tara.magritte.Layer as = a$(io.intino.ness.datahub.graph.adaptertype.AdapterTypeAdapter.class);
		return as != null ? (io.intino.ness.datahub.graph.adaptertype.AdapterTypeAdapter) as : null;
	}

	public boolean isAdapterType() {
		return core$().is(io.intino.ness.datahub.graph.adaptertype.AdapterTypeAdapter.class);
	}

	public io.intino.ness.datahub.graph.event.EventAdapter asEvent() {
		return a$(io.intino.ness.datahub.graph.event.EventAdapter.class);
	}

	public io.intino.ness.datahub.graph.event.EventAdapter asEvent(java.util.List<io.intino.ness.datahub.graph.Datalake.Tank> tanks, java.lang.String startTimetag, java.lang.String endTimetag, io.intino.ness.datahub.graph.functions.EventAdapter adapt) {
		io.intino.ness.datahub.graph.event.EventAdapter newElement = core$().addFacet(io.intino.ness.datahub.graph.event.EventAdapter.class);
		newElement.core$().set(newElement, "tanks", tanks);
		newElement.core$().set(newElement, "startTimetag", java.util.Collections.singletonList(startTimetag));
		newElement.core$().set(newElement, "endTimetag", java.util.Collections.singletonList(endTimetag));
		newElement.core$().set(newElement, "adapt", java.util.Collections.singletonList(adapt));
	    return newElement;
	}

	public boolean isEvent() {
		return core$().is(io.intino.ness.datahub.graph.event.EventAdapter.class);
	}

	public void removeEvent() {
		core$().removeFacet(io.intino.ness.datahub.graph.event.EventAdapter.class);
	}

	public io.intino.ness.datahub.graph.custom.CustomAdapter asCustom() {
		return a$(io.intino.ness.datahub.graph.custom.CustomAdapter.class);
	}

	public io.intino.ness.datahub.graph.custom.CustomAdapter asCustom(io.intino.ness.datahub.graph.functions.DatalakeAdapter adapt) {
		io.intino.ness.datahub.graph.custom.CustomAdapter newElement = core$().addFacet(io.intino.ness.datahub.graph.custom.CustomAdapter.class);
		newElement.core$().set(newElement, "adapt", java.util.Collections.singletonList(adapt));
	    return newElement;
	}

	public boolean isCustom() {
		return core$().is(io.intino.ness.datahub.graph.custom.CustomAdapter.class);
	}

	public void removeCustom() {
		core$().removeFacet(io.intino.ness.datahub.graph.custom.CustomAdapter.class);
	}

	public io.intino.ness.datahub.graph.realtime.RealtimeAdapter asRealtime() {
		io.intino.tara.magritte.Layer as = a$(io.intino.ness.datahub.graph.realtime.RealtimeAdapter.class);
		return as != null ? (io.intino.ness.datahub.graph.realtime.RealtimeAdapter) as : core$().addFacet(io.intino.ness.datahub.graph.realtime.RealtimeAdapter.class);
	}

	public boolean isRealtime() {
		return core$().is(io.intino.ness.datahub.graph.realtime.RealtimeAdapter.class);
	}

	@Override
	protected java.util.Map<java.lang.String, java.util.List<?>> variables$() {
		java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>();
		return map;
	}

	@Override
	protected void load$(java.lang.String name, java.util.List<?> values) {
		super.load$(name, values);
	}

	@Override
	protected void set$(java.lang.String name, java.util.List<?> values) {
		super.set$(name, values);
	}

	public io.intino.ness.datahub.graph.NessGraph graph() {
		return (io.intino.ness.datahub.graph.NessGraph) core$().graph().as(io.intino.ness.datahub.graph.NessGraph.class);
	}
}