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
	private List<String> tanks;
	private ReflowProcess reflowProcess;


	public ReflowProcessHandler(NessBox box, List<String> tanks) {
		this.graph = box.ness();
		this.tanks = tanks;
		this.reflowProcess = new ReflowProcess(box.datalakeManager(), box.busManager(), collectTanks());
	}

	boolean next(Integer size) {
		if (tanks.isEmpty()) return false;
		reflowProcess.next(size == 0 ? Integer.MAX_VALUE : size);
		return true;
	}

	boolean finished() {
		return reflowProcess.finished();
	}

	private List<Tank> collectTanks() {
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