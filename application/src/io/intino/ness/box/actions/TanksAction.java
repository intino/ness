package io.intino.ness.box.actions;

import io.intino.ness.graph.Tank;
import io.intino.ness.box.NessBox;
import io.intino.ness.box.slack.Helper;

import java.util.List;
import java.util.stream.Collectors;


public class TanksAction {

	public NessBox box;
	public java.util.List<String> tags;

	public List<String> execute() {
		return Helper.sortedTanks(box.ness()).map(Tank::qualifiedName).collect(Collectors.toList());
	}
}