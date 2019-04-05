package io.intino.ness.triton.box.actions;

import io.intino.ness.triton.box.TritonBox;
import io.intino.tara.magritte.Layer;

import java.util.List;
import java.util.stream.Collectors;

public class ExternalBusesAction {

	public TritonBox box;

	public List<String> execute() {
		return box.graph().externalBusList().stream().map(Layer::name$).collect(Collectors.toList());
	}
}