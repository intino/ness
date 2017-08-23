package io.intino.ness.box.actions;

import io.intino.ness.box.NessBox;
import io.intino.ness.graph.Tank;

import java.util.ArrayList;
import java.util.List;

import static io.intino.ness.box.slack.Helper.findTank;
import static org.apache.log4j.Logger.getRootLogger;


public class ReflowAction extends Action {

	public List<String> tanks;
	public NessBox box;

	public String execute() {
		List<Tank> realTanks = new ArrayList<>();
		for (String tank : tanks) {
			Tank realTank = findTank(box, tank);
			realTanks.add(realTank);
			if (realTank == null) return "Tank not found: " + tank;
		}
		getRootLogger().info("Starting reflow over " + String.join(", ", tanks));
		new Thread(() -> box.datalakeManager().reflow(realTanks)).start();
		return OK;
	}
}