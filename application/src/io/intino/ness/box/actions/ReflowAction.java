package io.intino.ness.box.actions;

import io.intino.ness.box.NessBox;
import io.intino.ness.graph.Tank;

import java.util.ArrayList;
import java.util.List;

import static io.intino.ness.box.slack.Helper.findTank;
import static java.util.Collections.emptyList;
import static org.apache.log4j.Logger.getRootLogger;


public class ReflowAction extends Action {

	public List<String> tanks;
	public NessBox box;

	public String execute() {
		List<Tank> tanks = collectTanks();
		if (tanks.isEmpty()) return "Tanks not found: " + String.join(", ", this.tanks);
		getRootLogger().info("Starting reflow over " + String.join(", ", this.tanks));
		new Thread(() -> box.datalakeManager().reflow(tanks)).start();
		return OK;
	}

	private List<Tank> collectTanks() {
		List<Tank> realTanks = new ArrayList<>();
		if (tanks.get(0).equalsIgnoreCase("all")) return box.ness().tankList();
		for (String tank : tanks) {
			Tank realTank = findTank(box, tank);
			realTanks.add(realTank);
			if (realTank == null) return emptyList();
		}
		return realTanks;
	}
}