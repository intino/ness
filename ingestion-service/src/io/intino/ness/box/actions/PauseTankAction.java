package io.intino.ness.box.actions;

import io.intino.alexandria.jms.TopicConsumer;
import io.intino.ness.box.NessServiceBox;
import io.intino.ness.box.Utils;
import io.intino.ness.datalake.Datalake.EventStore.Tank;
import io.intino.ness.datalake.Probes;

import java.util.List;

import static io.intino.ness.box.actions.Action.OK;

public class PauseTankAction {
	public NessServiceBox box;
	public String tank;

	public PauseTankAction() {
	}

	public PauseTankAction(NessServiceBox box, String tank) {
		this.box = box;
		this.tank = tank;
	}

	public String execute() {
		Tank datalakeTank = Utils.findTank(box.datalake(), tank);
		if (datalakeTank == null) return "tank not found";
		return execute(datalakeTank);
	}

	public String execute(Tank aTank) {
		io.intino.ness.graph.Datalake.Tank jmsTank = Utils.findTank(box.graph(), aTank.name());
		List<TopicConsumer> consumers = box.busManager().consumersOf(Probes.feed(aTank));
		consumers.forEach(TopicConsumer::stop);
		jmsTank.active(false);
		jmsTank.save$();
		return OK;
	}
}