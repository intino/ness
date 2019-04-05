package io.intino.ness.box.actions;

import io.intino.ness.box.NessServiceBox;
import io.intino.ness.graph.ExternalBus;
import io.intino.ness.graph.NessGraph;

import java.util.List;

import static io.intino.ness.box.actions.Action.OK;
import static java.util.stream.Collectors.toList;

public class RemoveExternalBusAction {

	public NessServiceBox box;
	public String name;

	public String execute() {
		List<ExternalBus> buses = ness().externalBusList(t -> t.name$().equals(name)).collect(toList());
		if (buses.isEmpty()) return "External bus not found";
		for (ExternalBus externalBus : buses) externalBus.delete$();
		return OK;
	}

	private NessGraph ness() {
		return box.graph();
	}
}