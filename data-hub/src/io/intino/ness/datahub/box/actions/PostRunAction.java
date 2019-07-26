package io.intino.ness.datahub.box.actions;

import io.intino.alexandria.Timetag;
import io.intino.alexandria.datalake.Datalake;
import io.intino.alexandria.exceptions.BadRequest;
import io.intino.alexandria.zim.ZimStream;
import io.intino.ness.datahub.box.DataHubBox;
import io.intino.ness.datahub.datalake.adapter.Context;
import io.intino.ness.datahub.graph.Adapter;
import io.intino.ness.datahub.graph.custom.CustomAdapter;
import io.intino.ness.datahub.graph.events.EventsAdapter;
import io.intino.ness.datahub.graph.sets.SetsAdapter;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class PostRunAction {

	public DataHubBox box;
	public io.intino.alexandria.core.Context context = new io.intino.alexandria.core.Context();
	public String name;
	public String configuration;
	public io.intino.alexandria.Resource attachment;

	public void execute() throws BadRequest {
		Adapter adapter = box.graph().adapterList().stream().filter(a -> a.name$().equals(name)).findFirst().orElse(null);
		if (adapter == null) throw new BadRequest("Adapter not found");
		if (adapter.isRealtime()) throw new BadRequest("Realtime Adapters cannot be run on demand");
		if (adapter.isEvents()) runEventAdapter(adapter.asEvents());
		if (adapter.isCustom()) runCustomAdapter(adapter.asCustom());
		else runSetAdapter(adapter);
	}

	private void runCustomAdapter(CustomAdapter adapter) {
		File adapterFolder = new File(box.adaptersFolder(), adapter.name$());
		new Thread(() -> adapter.adapt(box.datalake(), new Context(adapterFolder))).start();
	}

	private void runEventAdapter(EventsAdapter adapter) {
		ZimStream zim = new ZimStream.Merge(zimStreams(box.datalake().eventStore(), adapter));
		File adapterFolder = new File(box.adaptersFolder(), adapter.name$());
		adapterFolder.mkdirs();
		new Thread(() -> adapter.adapt(zim, new Context(adapterFolder))).start();
	}

	private void runSetAdapter(Adapter adapter) {
		SetsAdapter setAdapter = adapter.asSets();
//		ZimStream zim = new ZimStream.Merge(zimStreams(box.datalake().eventStore(), setAdapter));
//
//		File adapterFolder = new File(box.adaptersFolder(), adapter.name$());
//		new Thread(() -> setAdapter.adapt(zim, new Context(box.stageFolder(), adapterFolder, configuration, attachment.stream()))).start();
	}


	private ZimStream[] zimStreams(Datalake.EventStore store, EventsAdapter eventAdapter) {
		Timetag start = new Timetag(eventAdapter.startTimetag());
		Timetag end = new Timetag(eventAdapter.endTimetag());
		return store.tanks()
				.filter(tank -> eventAdapter.tanks().stream().anyMatch(t -> t.name().equals(tank.name())))
				.map(tank -> tank.content(ts -> (ts.isAfter(start) || ts.equals(start)) && (ts.isBefore(end) || ts.equals(end))))
				.toArray(ZimStream[]::new);
	}

	private List<Stream<Datalake.SetStore.Tub>> zetStreams(Datalake.SetStore store, SetsAdapter eventAdapter) {
		Timetag start = new Timetag(eventAdapter.startTimetag());
		Timetag end = new Timetag(eventAdapter.endTimetag());
		return store.tanks()
				.filter(tank -> eventAdapter.tanks().stream().anyMatch(t -> t.name().equals(tank.name())))
				.map(tank -> tank.tubs(start, end)).collect(Collectors.toList());
	}
}