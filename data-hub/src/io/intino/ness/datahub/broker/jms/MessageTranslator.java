package io.intino.ness.datahub.broker.jms;

import io.intino.alexandria.logger.Logger;
import io.intino.alexandria.message.Message;
import io.intino.alexandria.zim.ZimReader;

import javax.jms.TextMessage;

class MessageTranslator {

	static Message toInlMessage(javax.jms.Message message) {
		try {
			return readMessage(((TextMessage) message).getText());
		} catch (Throwable e) {
			Logger.error(e.getMessage(), e);
			return null;
		}
	}

	private static Message readMessage(String text) {
		return new ZimReader(text).next();
	}
}
