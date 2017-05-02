package io.intino.ness.konos.actions;

import io.intino.ness.Tank;
import io.intino.ness.konos.NessBox;

import java.util.List;
import java.util.stream.Collectors;

import static io.intino.ness.konos.slack.Helper.ness;
import static io.intino.ness.konos.slack.Helper.sortedTanks;


public class TanksAction {

	public NessBox box;
	public java.util.List<String> tags;

	public List<String> execute() {
		return sortedTanks(ness(box)).map(Tank::qualifiedName).collect(Collectors.toList());
	}
}