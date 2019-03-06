package io.intino.ness.box.actions;

import io.intino.ness.box.NessBox;
import io.intino.ness.graph.Tank;

import java.util.List;

import static java.util.stream.Collectors.toList;

public class TanksAction {

	public NessBox box;

	public List<String> execute() {
		return box.graph().tankList().stream().map(Tank::name).sorted(String.CASE_INSENSITIVE_ORDER).collect(toList());
	}
}