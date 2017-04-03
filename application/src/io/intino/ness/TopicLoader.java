package io.intino.ness;

import io.intino.ness.konos.TopicsBus;

import java.util.List;

public class TopicLoader {


	private final Ness ness;
	private final TopicsBus bus;

	public TopicLoader(Ness ness, TopicsBus bus) {
		this.ness = ness;
		this.bus = bus;
	}

	public void reload() {
		List<Topic> nessTopics = ness.topicList();
		for (String topic : this.bus.topics())
			if (nessTopics.stream().noneMatch(t -> t.name$().equals(topic))) {
				add(topic);
				System.out.println(topic);
			}
		bus.setListener(event -> {
			if (!event.getDestination().isTopic()) return;
			if (event.isAddOperation()) add(event.getDestination().getQualifiedName());
			else if (event.isRemoveOperation()) remove(event.getDestination().getQualifiedName());

		});
	}

	private void remove(String topic) {
		Topic toRemove = ness.topicList().stream().filter(t -> t.name$().equals(topic)).findFirst().orElse(null);
		if (toRemove != null) toRemove.delete();
	}

	private void add(String topic) {
		ness.create("topics").topic(topic);
	}
}
