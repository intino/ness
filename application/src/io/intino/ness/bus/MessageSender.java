package io.intino.ness.bus;

import io.intino.konos.jms.TopicProducer;
import io.intino.ness.graph.Function;
import io.intino.ness.inl.MessageFunction;
import io.intino.ness.inl.MessageMapper;
import io.intino.ness.inl.Text2TextMapper;
import io.intino.ness.inl.TextMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;

import static io.intino.konos.jms.MessageFactory.createMessageFor;

public class MessageSender {
	private static Logger logger = LoggerFactory.getLogger(MessageSender.class);

	public static void send(Session destination, String topic, Message message, Function function) {
		try {
			TopicProducer producer = new TopicProducer(destination, topic);
			String messageMapped = function == null ? textFrom(message) : mapToMessage(textFrom(message), function.aClass());
			if (!messageMapped.isEmpty()) producer.produce(createMessageFor(messageMapped));
			producer.close();
		} catch (JMSException e) {
			logger.error(e.getMessage(), e);
		}
	}

	private static String mapToMessage(String message, MessageFunction function) {
		if (function instanceof Text2TextMapper) {
			String newMessage = ((Text2TextMapper) function).map(message);
			return newMessage == null ? "" : newMessage;
		}
		if (function instanceof TextMapper) {
			io.intino.ness.inl.Message newMessage = ((TextMapper) function).map(message);
			return newMessage == null ? "" : newMessage.toString();
		}
		io.intino.ness.inl.Message newMessage = ((MessageMapper) function).map(io.intino.ness.inl.Message.load(message));
		return newMessage == null ? "" : newMessage.toString();
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
