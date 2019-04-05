package io.intino.ness.box.actions;

import io.intino.ness.box.NessServiceBox;
import io.intino.ness.graph.Datalake.Tank;

import java.util.List;

import static java.util.stream.Collectors.toList;

public class TanksAction {

	public NessServiceBox box;

	public List<String> execute() {
		return box.graph().datalake().tankList().stream().map(Tank::name).sorted(String.CASE_INSENSITIVE_ORDER).collect(toList());
	}
}