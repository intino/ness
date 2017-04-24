package io.intino.ness.konos.slack;

import io.intino.konos.slack.Bot.MessageProperties;
import io.intino.ness.DatalakeManager;
import io.intino.ness.Ness;
import io.intino.ness.Tank;
import io.intino.ness.bus.BusManager;
import io.intino.ness.konos.NessBox;

import java.util.Collections;

import static io.intino.ness.konos.slack.Helper.findTank;
import static io.intino.ness.konos.slack.Helper.ness;

public class TankSlack {

	private NessBox box;

	public TankSlack(NessBox box) {
		this.box = box;
	}

	public void init(com.ullink.slack.simpleslackapi.SlackSession session) {

	}

	public String tag(MessageProperties properties, String[] tags) {
		Tank tank = findTank(box, properties.context().getObjects()[0]);
		tank.tags().clear();
		Collections.addAll(tank.tags(), tags);
		return ":ok_hand:";
	}

	public String rename(MessageProperties properties, String name) {
		Tank tank = findTank(box, properties.context().getObjects()[0]);
		if (tank == null) return "Please select a tank";
		return box.get(BusManager.class).renameTopic(tank.qualifiedName(), name) ? ":ok_hand:" : "Impossible to rename tank";
	}

	public String seal(MessageProperties properties) {
		Ness ness = ness(box);
		Tank tank = findTank(box, properties.context().getObjects()[0]);
		datalake().seal(tank.qualifiedName());
		return ":ok_hand:";
	}

	private DatalakeManager datalake() {
		return box.get(DatalakeManager.class);
	}
}