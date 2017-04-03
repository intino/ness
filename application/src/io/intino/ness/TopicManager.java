package io.intino.ness;

import io.intino.ness.konos.TopicsBus;
import org.apache.activemq.ActiveMQConnection;

import java.util.List;

public class TopicManager {


	private final Ness ness;
	private final TopicsBus bus;

	public TopicManager(Ness ness, TopicsBus bus) {
		this.ness = ness;
		this.bus = bus;
	}

	public void reload() {
		List<Topic> nessTopics = ness.topicList();
		this.bus.topics().stream().filter(topic ->
				nessTopics.stream().noneMatch(t -> t.qualifiedName().equals(topic))).forEach(topic -> {
			add(topic);
			System.out.println(topic);
		});
		bus.setListener(event -> {
			if (!event.getDestination().isTopic()) return;
			if (event.isAddOperation()) add(event.getDestination().getQualifiedName());
			else if (event.isRemoveOperation()) remove(event.getDestination().getQualifiedName());
		});
		ness.graph().saveAll();
	}

	private void remove(String topic) {
		Topic toRemove = ness.topicList().stream().filter(t -> t.qualifiedName().equals(topic)).findFirst().orElse(null);
		if (toRemove != null) toRemove.delete();
		ActiveMQConnection connection = (ActiveMQConnection) bus.connection();
	}

	private void add(String topic) {
		ness.create("topics").topic(topic);
	}
}
