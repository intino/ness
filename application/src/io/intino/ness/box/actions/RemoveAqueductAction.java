package io.intino.ness.box.actions;

import io.intino.ness.box.NessBox;
import io.intino.ness.graph.Aqueduct;
import io.intino.ness.graph.NessGraph;

import java.util.List;

import static io.intino.ness.box.actions.Action.OK;
import static java.util.stream.Collectors.toList;


public class RemoveAqueductAction {

	public NessBox box;
	public String name;

	public String execute() {
		List<Aqueduct> aqueducts = ness().aqueductList(t -> t.name$().equals(name)).collect(toList());
		if (aqueducts.isEmpty()) return "Aqueduct not found";
		for (Aqueduct tank : aqueducts) {
			tank.delete$();
		}
		return OK;
	}

	private NessGraph ness() {
		return box.ness();
	}

}