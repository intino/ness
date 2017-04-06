package io.intino.ness.bus;

import io.intino.ness.Ness;
import io.intino.ness.Topic;
import io.intino.ness.konos.NessBox;
import org.apache.activemq.command.ActiveMQDestination;
import org.apache.activemq.command.ActiveMQMessage;
import org.apache.activemq.command.ActiveMQTopic;
import org.apache.activemq.command.DestinationInfo;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Session;
import java.util.ArrayList;
import java.util.List;

import static java.util.logging.Logger.getGlobal;

final class TopicListener implements MessageListener {

	private Ness ness;
	private NessBox nessBox;
	private Session nessSession;
	private List<FeedListener> listeners = new ArrayList<>();

	TopicListener(NessBox nessBox, Session nessSession) {
		this.nessBox = nessBox;
		this.ness = nessBox.graph().wrapper(Ness.class);
		this.nessSession = nessSession;
	}

	@Override
	public void onMessage(Message message) {
		if (message instanceof ActiveMQMessage) {
			DestinationInfo info = (DestinationInfo) ((ActiveMQMessage) message).getDataStructure();
			if (info.isAddOperation()) addTopic(info.getDestination());
			else if (info.isRemoveOperation()) removeTopic(info.getDestination());
		}
	}

	private void addTopic(ActiveMQDestination destination) {
		Topic topic = findTopic(destination);
		if (topic == null) ness.create("topics").topic(destination.getPhysicalName()).save();
		if (destination.getPhysicalName().startsWith("feed.")) addFeedListener((ActiveMQTopic) destination);
	}

	private void removeTopic(ActiveMQDestination destination) {
		Topic topic = findTopic(destination);
		if (topic == null) return;
		topic.delete();
	}

	private void addFeedListener(ActiveMQTopic topic) {
		try {
			FeedListener feedListener = new FeedListener(nessBox, ness, topic.getPhysicalName());
			nessSession.createDurableSubscriber(topic, BusManager.NESS).setMessageListener(feedListener);
			listeners.add(feedListener);
		} catch (JMSException e) {
			getGlobal().severe(e.getMessage());
		}
	}

	private Topic findTopic(ActiveMQDestination destination) {
		List<Topic> topics = ness.topicList(t -> t.qualifiedName().equals(destination.getPhysicalName()));
		return topics.isEmpty() ? null : topics.get(0);
	}
}
