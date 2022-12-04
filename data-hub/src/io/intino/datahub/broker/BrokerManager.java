package io.intino.datahub.broker;

import io.intino.alexandria.jms.QueueConsumer;
import io.intino.alexandria.jms.QueueProducer;
import io.intino.alexandria.jms.TopicConsumer;
import io.intino.alexandria.jms.TopicProducer;

import javax.jms.Message;
import javax.jms.Session;
import java.util.function.Consumer;

public interface BrokerManager {

	TopicConsumer registerTopicConsumer(String topic, Consumer<Message> consumer);

	QueueConsumer registerQueueConsumer(String topic, Consumer<Message> consumer);

	void unregisterConsumer(TopicConsumer consumer);

	TopicProducer topicProducerOf(String topic);

	QueueProducer queueProducerOf(String queue);

	Session session();
}
