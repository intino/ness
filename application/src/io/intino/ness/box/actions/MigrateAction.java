package io.intino.ness.box.actions;

import io.intino.ness.graph.Function;
import io.intino.ness.graph.Tank;
import io.intino.ness.box.NessBox;
import io.intino.ness.box.slack.Helper;

import java.util.List;

import static java.util.stream.Collectors.toList;


public class MigrateAction extends Action {
	public NessBox box;
	public String tank;
	public java.util.List<String> functions;

	public String execute() {
		List<Function> functions = this.functions.stream().map(a -> box.ness().functionList().stream().filter(f -> f.name$().equals(a)).findFirst().orElse(null)).collect(toList());
		if (functions.contains(null))
			return "Function " + this.functions.get(functions.indexOf(null)) + " not found. Please, register it first";
		Tank tank = Helper.findTank(box, this.tank);
		String newTankName = nextVersionOf(tank);
		Tank newTank = box.ness().create("tanks", newTankName).tank(newTankName);
		try {
			box.datalakeManager().migrate(tank, newTank, functions);
		} catch (Exception e) {
			return "Migration failed: " + e.getMessage();
		}
		newTank.save$();
		return OK;
	}

	private String nextVersionOf(Tank tank) {
		return tank.qualifiedName().replace("." + tank.version(), "." + (tank.version() + 1));
	}

}