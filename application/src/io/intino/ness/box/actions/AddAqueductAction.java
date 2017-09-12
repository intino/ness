package io.intino.ness.box.actions;

import io.intino.ness.box.NessBox;
import io.intino.ness.graph.Aqueduct;
import io.intino.ness.graph.ExternalBus;
import io.intino.ness.graph.Function;

import static io.intino.ness.box.actions.Action.OK;


public class AddAqueductAction {

	public NessBox box;
	public String name;
	public String externalBus;
	public String direction;
	public String functionName;
	public String tankMacro;

	public String execute() {
		ExternalBus bus = box.ness().externalBusList(f -> f.name$().equals(externalBus)).findFirst().orElse(null);
		if (bus == null) return "External Bus not found";
		Function function = box.ness().functionList(f -> f.name$().equals(functionName)).findFirst().orElse(null);
		if (function == null) return "Function not found";
		Aqueduct aqueduct = box.ness().aqueductList(f -> f.name$().equals(name)).findFirst().orElse(null);
		if (aqueduct != null) return "Aqueduct is already defined";
		box.ness().create("aqueducts", name).aqueduct(Aqueduct.Direction.valueOf(direction), bus, function, tankMacro).save$();
		return OK;
	}
}