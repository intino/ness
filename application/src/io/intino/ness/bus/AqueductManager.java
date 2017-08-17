package io.intino.ness.bus;

import io.intino.konos.jms.TopicConsumer;
import io.intino.konos.jms.TopicProducer;
import io.intino.ness.datalake.compiler.Compiler;
import io.intino.ness.graph.Aqueduct;
import io.intino.ness.inl.MessageFunction;
import io.intino.ness.inl.MessageMapper;
import io.intino.ness.inl.Text2TextMapper;
import io.intino.ness.inl.TextMapper;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.ActiveMQSession;
import org.apache.activemq.command.ActiveMQMessage;
import org.apache.activemq.command.DestinationInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;
import java.util.*;
import java.util.regex.Pattern;

import static io.intino.konos.jms.MessageFactory.createMessageFor;
import static io.intino.ness.graph.Aqueduct.Direction.incoming;
import static java.lang.Thread.sleep;
import static java.util.stream.Collectors.toList;
import static javax.jms.Session.AUTO_ACKNOWLEDGE;
import static org.slf4j.Logger.ROOT_LOGGER_NAME;

public class AqueductManager {
	private static final Logger logger = LoggerFactory.getLogger(ROOT_LOGGER_NAME);
	private final Aqueduct aqueduct;
	private Session ness;
	private final Session externalBus;
	private final MessageFunction function;
	private final List<String> nessTopics;
	private final List<TopicConsumer> topicConsumers;
	private final BusManager busManager;
	private Session session;

	public AqueductManager(Aqueduct aqueduct, BusManager busManager) {
		this.aqueduct = aqueduct;
		this.busManager = busManager;
		this.ness = busManager.nessSession();
		this.nessTopics = busManager.topics();
		this.externalBus = initOriginSession();
		this.function = map(aqueduct.transformer().qualifiedName(), aqueduct.transformer().source());
		this.topicConsumers = new ArrayList<>();
	}

	public void start() {
		if (aqueduct.direction().equals(incoming)) {
			for (String topic : filter(externalBusTopics(), aqueduct.tankMacro())) {
				TopicConsumer consumer = new TopicConsumer(externalBus, topic);
				consumer.listen(m -> sendTo(ness, topic, m));
				topicConsumers.add(consumer);
			}
		} else {
			for (String topic : filter(nessTopics, aqueduct.tankMacro())) {
				if (((ActiveMQSession) ness).isClosed()) this.ness = busManager.nessSession();
				TopicConsumer consumer = new TopicConsumer(ness, topic);
				consumer.listen(m -> sendTo(externalBus, topic, m));
				topicConsumers.add(consumer);
			}
		}
	}

	private void sendTo(Session destination, String topic, Message message) {
		try {
			TopicProducer producer = new TopicProducer(destination, topic);
			String messageMapped = mapToMessage(textFrom(message));
			producer.produce(createMessageFor(messageMapped));
		} catch (JMSException e) {
			logger.error(e.getMessage(), e);
		}
	}

	private Collection<String> filter(Collection<String> topics, String macro) {
		Pattern pattern = Pattern.compile(macro);
		return topics.stream().filter(t -> pattern.matcher(t).matches()).collect(toList());
	}

	public void stop() {
		try {
			TopicConsumer consumer = null;
			for (TopicConsumer topicConsumer : topicConsumers) {
				consumer = topicConsumer;
				topicConsumer.stop();
			}
			if (consumer != null) topicConsumers.remove(consumer);
			session.close();
		} catch (JMSException e) {
			logger.error(e.getMessage(), e);
		}
	}

	private Session initOriginSession() {
		try {
			ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(aqueduct.bus().originURL());
			javax.jms.Connection connection = connectionFactory.createConnection(aqueduct.bus().user(), aqueduct.bus().password());
			session = connection.createSession(false, AUTO_ACKNOWLEDGE);
			connection.start();
			return session;
		} catch (JMSException e) {
			logger.error(e.getMessage(), e);
			return null;
		}
	}

	private Collection<String> externalBusTopics() {
		Set<String> topics = new HashSet<>();
		try {
			MessageConsumer consumer = session.createConsumer(session.createTopic("ActiveMQ.Advisory.Topic"));
			consumer.setMessageListener(message -> {
				ActiveMQMessage m = (ActiveMQMessage) message;
				if (m.getDataStructure() instanceof DestinationInfo) {
					topics.add(((DestinationInfo) m.getDataStructure()).getDestination().getPhysicalName());
				}
			});
			sleep(1000);
			consumer.close();
		} catch (JMSException | InterruptedException e) {
			logger.error(e.getMessage(), e);
		}
		return filter(topics, aqueduct.tankMacro());
	}

	private String mapToMessage(String message) {
		if (function instanceof TextMapper) return ((TextMapper) function).map(message).toString();
		if (function instanceof Text2TextMapper) return ((Text2TextMapper) function).map(message);
		else return ((MessageMapper) function).map(io.intino.ness.inl.Message.load(message)).toString();
	}

	private Compiler.Result compile(String function, String... sources) {
		return Compiler.compile(sources)
				.with("-target", "1.8")
				.load(function);
	}

	private MessageFunction map(String function, String... sources) {
		try {
			return map(compile(function, sources).as(MessageFunction.class));
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return null;
		}
	}

	private MessageFunction map(Class<? extends MessageFunction> mapperClass) throws Exception {
		return mapperClass.newInstance();
	}

	private static String textFrom(Message message) {
		try {
			if (message instanceof BytesMessage) {
				byte[] data = new byte[(int) ((BytesMessage) message).getBodyLength()];
				((BytesMessage) message).readBytes(data);
				return new String(data);
			} else return ((TextMessage) message).getText();
		} catch (JMSException e) {
			logger.error(e.getMessage(), e);
			return "";
		}
	}
}
