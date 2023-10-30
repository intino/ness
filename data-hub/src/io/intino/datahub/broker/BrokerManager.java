package io.intino.datahub.broker;

import io.intino.alexandria.jms.QueueProducer;
import io.intino.alexandria.jms.TopicConsumer;
import io.intino.alexandria.jms.TopicProducer;

import javax.jms.Message;
import javax.jms.Session;
import java.util.function.Consumer;

public interface BrokerManager {

	void registerTopicConsumer(String topic, Consumer<Message> consumer);

	void registerQueueConsumer(String topic, Consumer<Message> consumer);

	void startTankConsumers();

	void pauseTankConsumers();

	void unregisterConsumer(String topic);

	void unregisterConsumer(TopicConsumer consumer);

	void unregisterQueueProducer(String destination);

	TopicProducer topicProducerOf(String topic);

	QueueProducer queueProducerOf(String queue);

	Session session();
}
