package io.intino.ness.triton.box.actions;

import io.intino.ness.triton.box.TritonBox;
import io.intino.ness.triton.graph.ExternalBus;
import io.intino.ness.triton.graph.NessGraph;

import java.util.List;

import static java.util.stream.Collectors.toList;

public class RemoveExternalBusAction {

	public TritonBox box;
	public String name;

	public String execute() {
		List<ExternalBus> buses = ness().externalBusList(t -> t.name$().equals(name)).collect(toList());
		if (buses.isEmpty()) return "External bus not found";
		for (ExternalBus externalBus : buses) externalBus.delete$();
		return Action.OK;
	}

	private NessGraph ness() {
		return box.graph();
	}
}