package io.intino.ness.konos.slack;

import io.intino.konos.slack.Bot.MessageProperties;
import io.intino.ness.Channel;
import io.intino.ness.Ness;
import io.intino.ness.bus.BusManager;
import io.intino.ness.konos.NessBox;

import java.util.Collections;

import static io.intino.ness.konos.slack.Helper.findChannel;
import static io.intino.ness.konos.slack.Helper.ness;

public class ChannelSlack {

	private NessBox box;

	public ChannelSlack(NessBox box) {
		this.box = box;
	}

	public void init(com.ullink.slack.simpleslackapi.SlackSession session) {

	}

	public String tag(MessageProperties properties, String[] tags) {
		Channel topic = findChannel(box, properties.context().getObjects()[0]);
		topic.tags().clear();
		Collections.addAll(topic.tags(), tags);
		return ":ok_hand:";
	}

	public String rename(MessageProperties properties, String name) {
		Channel topic = findChannel(box, properties.context().getObjects()[0]);
		if (topic == null) return "Please select a topic";
		return box.get(BusManager.class).renameTopic(topic.qualifiedName(), name) ? ":ok_hand:" : "Impossible to rename topic";
	}

	public String consolidate(MessageProperties properties) {
		Ness ness = ness(box);
		return "TODO";
	}
}