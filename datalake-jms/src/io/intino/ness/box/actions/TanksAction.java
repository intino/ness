package io.intino.ness.box.actions;

import io.intino.ness.box.NessBox;
import io.intino.ness.box.slack.Helper;
import io.intino.ness.datalake.graph.Tank;

import java.util.List;
import java.util.stream.Collectors;

public class TanksAction {

	public NessBox box;

	public List<String> execute() {
		return Helper.sortedTanks(box.datalake()).map(Tank::qualifiedName).collect(Collectors.toList());
	}
}