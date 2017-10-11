package io.intino.ness.datalake.reflow;

import io.intino.ness.box.NessBox;
import io.intino.ness.graph.NessGraph;
import io.intino.ness.graph.Tank;

import java.util.ArrayList;
import java.util.List;

import static io.intino.ness.box.slack.Helper.findTank;
import static java.util.Collections.emptyList;


public class ReflowProcessHandler {

	private final NessGraph graph;
	private List<Tank> tanks;
	private ReflowProcess reflowProcess;

	ReflowProcessHandler(NessBox box, List<String> tanks, Integer blockSize) {
		this.graph = box.ness();
		this.tanks = collectTanks(tanks);
		this.reflowProcess = new ReflowProcess(box.datalakeManager(), box.busManager(), this.tanks, blockSize == 0 ? Integer.MAX_VALUE : blockSize);
	}

	boolean next() {
		if (tanks.isEmpty()) return false;
		reflowProcess.next();
		return true;
	}

	public List<Tank> tanks() {
		return tanks;
	}

	private List<Tank> collectTanks(List<String> tanks) {
		List<Tank> realTanks = new ArrayList<>();
		if (tanks.get(0).equalsIgnoreCase("all")) return graph.tankList();
		for (String tank : tanks) {
			Tank realTank = findTank(graph, tank);
			realTanks.add(realTank);
			if (realTank == null) return emptyList();
		}
		return realTanks;
	}
}