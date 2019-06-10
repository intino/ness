package io.intino.ness.triton.datalake;

import io.intino.alexandria.inl.Message;
import io.intino.alexandria.logger.Logger;
import io.intino.alexandria.zim.ZimReader;

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
		return new ZimReader(text).next();
	}
}
