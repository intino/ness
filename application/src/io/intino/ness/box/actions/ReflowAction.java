package io.intino.ness.box.actions;

import io.intino.ness.box.NessBox;
import io.intino.ness.box.slack.Helper;
import io.intino.ness.graph.Tank;

import java.util.ArrayList;
import java.util.List;


public class ReflowAction extends Action {

	public List<String> tanks;
	public NessBox box;

	public String execute() {
		List<Tank> realTanks = new ArrayList<>();
		for (String tank : tanks) {
			Tank realTank = Helper.findTank(box, tank);
			realTanks.add(realTank);
			if (realTank == null) return "Tank not found: " + tank;
		}
		new Thread(() -> box.datalakeManager().reflow(realTanks)).start();
		return OK;
	}
}