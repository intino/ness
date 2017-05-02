package io.intino.ness.konos.actions;

import io.intino.ness.Function;
import io.intino.ness.Tank;
import io.intino.ness.konos.NessBox;

import java.util.List;
import java.util.stream.Collectors;

import static io.intino.ness.konos.slack.Helper.findTank;
import static io.intino.ness.konos.slack.Helper.ness;


public class MigrateAction extends Action {
	public NessBox box;
	public String tank;
	public java.util.List<String> functions;

	public String execute() {
		List<Function> functions = this.functions.stream().map(a -> ness(box).functionList().stream().filter(f -> f.name().equals(a)).findFirst().orElse(null)).collect(Collectors.toList());
		if (functions.contains(null))
			return "Function " + this.functions.get(functions.indexOf(null)) + " not found. Please, register it first";
		Tank tank = findTank(box, this.tank);
		String newTankName = nextVersionOf(tank);
		Tank newTank = ness(box).create("tanks", newTankName).tank(newTankName);
		try {
			datalake(box).migrate(tank, newTank, functions);
		} catch (Exception e) {
			return "Migration failed: " + e.getMessage();
		}
		newTank.save();
		return OK;
	}

	private String nextVersionOf(Tank tank) {
		return tank.qualifiedName().replace("." + tank.version(), "." + (tank.version() + 1));
	}

}