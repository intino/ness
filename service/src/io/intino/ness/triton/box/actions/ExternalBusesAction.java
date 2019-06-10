package io.intino.ness.triton.box.actions;

import io.intino.ness.triton.box.ServiceBox;
import io.intino.tara.magritte.Layer;

import java.util.List;
import java.util.stream.Collectors;

public class ExternalBusesAction {

	public ServiceBox box;

	public List<String> execute() {
		return box.graph().externalBusList().stream().map(Layer::name$).collect(Collectors.toList());
	}
}