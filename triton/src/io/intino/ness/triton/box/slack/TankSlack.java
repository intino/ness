package io.intino.ness.triton.box.slack;

import io.intino.alexandria.slack.Bot;
import io.intino.ness.triton.box.TritonBox;
import io.intino.ness.triton.box.actions.RenameTankAction;

public class TankSlack {
	private static final String OK = ":ok_hand:";

	private TritonBox box;

	public TankSlack(TritonBox box) {
		this.box = box;
	}

	public void init(com.ullink.slack.simpleslackapi.SlackSession session) {

	}

	public String rename(Bot.MessageProperties properties, String name) {
		RenameTankAction action = new RenameTankAction();
		action.box = box;
		action.tank = properties.context().getObjects()[0];
		action.name = name;
		return action.execute();
	}
}