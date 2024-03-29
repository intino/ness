package io.intino.datahub.broker.jms;

import io.intino.alexandria.logger.Logger;
import io.intino.alexandria.message.Message;
import io.intino.alexandria.message.MessageReader;
import jakarta.jms.MessageNotWriteableException;
import jakarta.jms.TextMessage;
import org.apache.activemq.command.ActiveMQTextMessage;

import java.util.Collections;
import java.util.Iterator;

public class JmsMessageTranslator {

	public static Iterator<Message> toInlMessages(jakarta.jms.Message message) {
		try {
			return readMessages(((TextMessage) message).getText());
		} catch (Throwable e) {
			Logger.error(e.getMessage(), e);
			return Collections.emptyIterator();
		}
	}

	private static Iterator<Message> readMessages(String text) {
		return new MessageReader(text).iterator();
	}

	public static jakarta.jms.Message toJmsMessage(String message) {
		ActiveMQTextMessage activeMQTextMessage = new ActiveMQTextMessage();
		try {
			activeMQTextMessage.setText(message);
			return activeMQTextMessage;
		} catch (MessageNotWriteableException e) {
			Logger.error(e);
			return null;
		}
	}

}
