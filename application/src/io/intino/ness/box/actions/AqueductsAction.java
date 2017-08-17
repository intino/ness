package io.intino.ness.box.actions;

import io.intino.ness.box.NessBox;
import io.intino.tara.magritte.Layer;

import java.util.List;
import java.util.stream.Collectors;


public class AqueductsAction {

	public NessBox box;

	public List<String> execute() {
		return box.ness().aqueductList().stream().map(Layer::name$).collect(Collectors.toList());
	}
}