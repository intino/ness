package io.intino.ness.triton.box.actions;

import io.intino.alexandria.jms.TopicConsumer;
import io.intino.ness.datalake.Datalake.EventStore.Tank;
import io.intino.ness.triton.box.TritonBox;
import io.intino.ness.triton.box.Utils;
import io.intino.ness.triton.datalake.Probes;
import io.intino.ness.triton.graph.Datalake;

import java.util.List;

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
		Tank datalakeTank = Utils.findTank(box.datalake(), tank);
		if (datalakeTank == null) return "tank not found";
		return execute(datalakeTank);
	}

	public String execute(Tank aTank) {
		Datalake.Tank jmsTank = Utils.findTank(box.graph(), aTank.name());
		List<TopicConsumer> consumers = box.busManager().consumersOf(Probes.feed(aTank));
		consumers.forEach(TopicConsumer::stop);
		jmsTank.active(false);
		jmsTank.save$();
		return Action.OK;
	}
}