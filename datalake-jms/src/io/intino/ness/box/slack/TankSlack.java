package io.intino.ness.box.slack;

import io.intino.konos.slack.Bot.MessageProperties;
import io.intino.ness.box.NessBox;
import io.intino.ness.box.actions.RenameTankAction;
import io.intino.ness.box.actions.SealTanksAction;
import io.intino.ness.datalake.graph.Tank;

import java.util.Collections;

public class TankSlack {
	private static final String OK = ":ok_hand:";

	private NessBox box;

	public TankSlack(NessBox box) {
		this.box = box;
	}

	public void init(com.ullink.slack.simpleslackapi.SlackSession session) {

	}

	public String rename(MessageProperties properties, String name) {
		RenameTankAction action = new RenameTankAction();
		action.box = box;
		action.tank = properties.context().getObjects()[0];
		action.name = name;
		return action.execute();
	}
}