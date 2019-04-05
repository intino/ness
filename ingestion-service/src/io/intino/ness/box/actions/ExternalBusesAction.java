package io.intino.ness.box.actions;

import io.intino.ness.box.NessServiceBox;
import io.intino.tara.magritte.Layer;

import java.util.List;
import java.util.stream.Collectors;

public class ExternalBusesAction {

	public NessServiceBox box;

	public List<String> execute() {
		return box.graph().externalBusList().stream().map(Layer::name$).collect(Collectors.toList());
	}
}