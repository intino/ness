package io.intino.datahub.broker.jms;

import io.intino.alexandria.logger.Logger;
import io.intino.alexandria.message.Message;
import io.intino.alexandria.message.MessageReader;
import org.apache.activemq.command.ActiveMQTextMessage;

import javax.jms.MessageNotWriteableException;
import javax.jms.TextMessage;

public class MessageTranslator {

	public static Message toInlMessage(javax.jms.Message message) {
		try {
			return readMessage(((TextMessage) message).getText());
		} catch (Throwable e) {
			Logger.error(e.getMessage(), e);
			return null;
		}
	}

	private static Message readMessage(String text) {
		return new MessageReader(text).next();
	}

	public static javax.jms.Message toJmsMessage(String message) {
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
