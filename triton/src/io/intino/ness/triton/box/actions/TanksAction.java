package io.intino.ness.triton.box.actions;

import io.intino.ness.triton.box.TritonBox;
import io.intino.ness.triton.graph.Tank;

import java.util.List;

import static java.util.stream.Collectors.toList;

public class TanksAction {

	public TritonBox box;

	public List<String> execute() {
		return box.graph().tankList().stream().map(Tank::name).sorted(String.CASE_INSENSITIVE_ORDER).collect(toList());
	}
}