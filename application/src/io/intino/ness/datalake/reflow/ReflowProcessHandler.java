package io.intino.ness.datalake.reflow;

import io.intino.ness.box.NessBox;
import io.intino.ness.graph.NessGraph;
import io.intino.ness.graph.Tank;

import javax.jms.Session;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static io.intino.ness.box.slack.Helper.findTank;


public class ReflowProcessHandler {

	private List<Tank> tanks;
	private ReflowProcess reflowProcess;

	ReflowProcessHandler(NessBox box, List<String> tanks, Instant from, Integer blockSize) {
		this.tanks = collectTanks(box.graph(), tanks);
		this.reflowProcess = new ReflowProcess(box.datalakeManager(), box.busManager(), this.tanks, from, blockSize == 0 ? Integer.MAX_VALUE : blockSize);
	}

	void next() {
		if (!tanks.isEmpty()) reflowProcess.next();
	}

	public Session session() {
		return reflowProcess.getSession();
	}

	public List<Tank> tanks() {
		return tanks;
	}

	private List<Tank> collectTanks(NessGraph graph, List<String> tanks) {
		List<Tank> realTanks = new ArrayList<>();
		if (tanks.get(0).equalsIgnoreCase("all")) return graph.tankList();
		for (String tank : tanks) {
			Tank realTank = findTank(graph, tank);
			if (realTank != null) realTanks.add(realTank);
		}
		return realTanks;
	}
}