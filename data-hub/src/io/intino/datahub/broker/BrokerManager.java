package io.intino.datahub.broker;

import io.intino.alexandria.jms.QueueConsumer;
import io.intino.alexandria.jms.TopicConsumer;
import io.intino.alexandria.jms.TopicProducer;

import javax.jms.Message;
import java.util.function.Consumer;

public interface BrokerManager {

	TopicConsumer registerTopicConsumer(String topic, Consumer<Message> consumer);

	QueueConsumer registerQueueConsumer(String topic, Consumer<Message> consumer);

	void unregisterConsumer(TopicConsumer consumer);

	TopicProducer producerOf(String topic);
}
