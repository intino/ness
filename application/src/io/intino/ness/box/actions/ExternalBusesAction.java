package io.intino.ness.box.actions;

import io.intino.ness.box.NessBox;

import java.util.List;
import java.util.stream.Collectors;


public class ExternalBusesAction {

	public NessBox box;


	public List<String> execute() {
		return box.ness().externalBusList().stream().map(e -> e.name$()).collect(Collectors.toList());
	}
}