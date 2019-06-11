package io.intino.ness.triton.box.actions;

import io.intino.alexandria.Timetag;
import io.intino.alexandria.exceptions.BadRequest;
import io.intino.alexandria.zim.ZimStream;
import io.intino.ness.datalake.Datalake.EventStore;
import io.intino.ness.datalake.Datalake.SetStore;
import io.intino.ness.triton.box.ServiceBox;
import io.intino.ness.triton.datalake.adapter.Context;
import io.intino.ness.triton.graph.Adapter;
import io.intino.ness.triton.graph.event.EventAdapter;
import io.intino.ness.triton.graph.set.SetAdapter;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class PostAdapterAction {

	public ServiceBox box;
	public io.intino.alexandria.core.Context context = new io.intino.alexandria.core.Context();
	public String name;
	public String configuration;
	public io.intino.alexandria.Resource attachment;

	public void execute() throws BadRequest {
		Adapter adapter = box.graph().adapterList().stream().filter(a -> a.name$().equals(name)).findFirst().orElse(null);
		if (adapter == null) throw new BadRequest("Adapter not found");
		if (adapter.isRealtime()) throw new BadRequest("Realtime Adapters cannot be run on demand");
		if (adapter.isEvent()) runEventAdapter(adapter);
		else runSetAdapter(adapter);
	}

	private void runEventAdapter(Adapter adapter) {
		EventAdapter eventAdapter = adapter.asEvent();
		ZimStream zim = new ZimStream.Merge(zimStreams(box.datalake().eventStore(), eventAdapter));
		File adapterFolder = new File(box.adaptersFolder(), adapter.name$());
		new Thread(() -> eventAdapter.adapt(zim, new Context(box.stageFolder(), adapterFolder, configuration, attachment.stream()))).start();
	}

	private void runSetAdapter(Adapter adapter) {
		SetAdapter setAdapter = adapter.asSet();
//		ZimStream zim = new ZimStream.Merge(zimStreams(box.datalake().eventStore(), setAdapter));
//
//		File adapterFolder = new File(box.adaptersFolder(), adapter.name$());
//		new Thread(() -> setAdapter.adapt(zim, new Context(box.stageFolder(), adapterFolder, configuration, attachment.stream()))).start();
	}


	private ZimStream[] zimStreams(EventStore store, EventAdapter eventAdapter) {
		Timetag start = new Timetag(eventAdapter.startTimetag());
		Timetag end = new Timetag(eventAdapter.endTimetag());
		return store.tanks()
				.filter(tank -> eventAdapter.tanks().stream().anyMatch(t -> t.name().equals(tank.name())))
				.map(tank -> tank.content(ts -> (ts.isAfter(start) || ts.equals(start)) && (ts.isBefore(end) || ts.equals(end))))
				.toArray(ZimStream[]::new);
	}

	private List<Stream<SetStore.Tub>> zetStreams(SetStore store, SetAdapter eventAdapter) {
		Timetag start = new Timetag(eventAdapter.startTimetag());
		Timetag end = new Timetag(eventAdapter.endTimetag());
		return store.tanks()
				.filter(tank -> eventAdapter.tanks().stream().anyMatch(t -> t.name().equals(tank.name())))
				.map(tank -> tank.tubs(start, end)).collect(Collectors.toList());
	}
}