package io.intino.ness.triton.box.actions;

import io.intino.alexandria.jms.TopicConsumer;
import io.intino.ness.datalake.Datalake.EventStore.Tank;
import io.intino.ness.triton.box.TritonBox;
import io.intino.ness.triton.datalake.Probes;

import java.util.List;

import static io.intino.ness.triton.box.Utils.findTank;

public class PauseTankAction {
	public TritonBox box;
	public String tank;

	public PauseTankAction() {
	}

	public PauseTankAction(TritonBox box, String tank) {
		this.box = box;
		this.tank = tank;
	}

	public String execute() {
		Tank datalakeTank = findTank(box.datalake(), tank);
		if (datalakeTank == null) return "tank not found";
		return execute(datalakeTank);
	}

	public String execute(Tank aTank) {
		io.intino.ness.triton.graph.Tank jmsTank = findTank(box.graph(), aTank.name());
		List<TopicConsumer> consumers = box.busManager().consumersOf(Probes.feed(aTank));
		consumers.forEach(TopicConsumer::stop);
		jmsTank.active(false);
		jmsTank.save$();
		return Action.OK;
	}
}