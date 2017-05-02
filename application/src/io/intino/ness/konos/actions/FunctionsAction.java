package io.intino.ness.konos.actions;

import io.intino.ness.konos.NessBox;
import io.intino.tara.magritte.Layer;

import java.util.List;
import java.util.stream.Collectors;

import static io.intino.ness.konos.slack.Helper.ness;


public class FunctionsAction {

	public NessBox box;


	public List<String> execute() {
		return ness(box).functionList().stream().map(Layer::name).collect(Collectors.toList());
	}
}