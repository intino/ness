package io.intino.ness.datalake;


import io.intino.alexandria.inl.Message;
import io.intino.alexandria.jms.MessageFactory;
import io.intino.alexandria.logger.Logger;
import io.intino.alexandria.zim.ZimReader;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.TextMessage;
import java.util.Collections;
import java.util.Map;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toMap;

public class MessageTranslator {

	private static String ATTACHMENT_IDS = "__attachment-ids__";
	private static String ATTACHMENT_SIZES = "__attachment-sizes__";
	private static String MESSAGE = "__text-message__";

	public static Message toInlMessage(javax.jms.Message message) {
		try {
			if (message instanceof BytesMessage) {
				final Message result = readMessage(message.getStringProperty(MESSAGE));
				Map<String, Integer> attachments = loadAttachmentProperties(message);
				for (String id : attachments.keySet()) {
					byte[] array = new byte[attachments.get(id)];
					((BytesMessage) message).readBytes(array, attachments.get(id));
					findAttachment(result, id).data(array);
				}
				return result;
			} else return readMessage(((TextMessage) message).getText());
		} catch (Throwable e) {
			Logger.error(e.getMessage(), e);
			return null;
		}
	}

	private static Message readMessage(String text) {
		return new ZimReader(text).next();
	}

	private static Message.Attachment findAttachment(Message message, String id) {
		Message.Attachment attachment = message.attachment(id);
		if (attachment != null) return attachment;
		for (Message component : message.components()) {
			attachment = findAttachment(component, id);
			if (attachment != null) return attachment;
		}
		return null;
	}

	public static javax.jms.Message fromInlMessage(Message message) {
		if (!message.attachments().isEmpty()) {
			javax.jms.Message result = MessageFactory.byteMessage();
			addAttachments((BytesMessage) result, message);
			addTextMessage(message, (BytesMessage) result);
			return result;
		} else return MessageFactory.createMessageFor(message.toString());
	}

	private static Map<String, Integer> loadAttachmentProperties(javax.jms.Message message) {
		try {
			if (!message.propertyExists(ATTACHMENT_IDS)) return Collections.emptyMap();
			final String[] ids = message.getStringProperty(ATTACHMENT_IDS).split(",");
			final String[] sizes = message.getStringProperty(ATTACHMENT_SIZES).split(",");
			return IntStream.range(0, ids.length).boxed().collect(toMap(i -> ids[i], i -> Integer.parseInt(sizes[i])));
		} catch (JMSException e) {
			Logger.error(e.getMessage(), e);
			return Collections.emptyMap();
		}
	}

	private static void addTextMessage(Message message, BytesMessage result) {
		try {
			result.setStringProperty(MESSAGE, message.toString());
		} catch (JMSException e) {
			Logger.error(e.getMessage(), e);
		}
	}

	private static void addAttachments(BytesMessage bytesMessage, Message message) {
		try {
			bytesMessage.setStringProperty(ATTACHMENT_IDS, String.join(",", message.attachments().stream().map(Message.Attachment::id).toArray(String[]::new)));
			bytesMessage.setStringProperty(ATTACHMENT_SIZES, String.join(",", message.attachments().stream().map(a -> a.data().length + "").toArray(String[]::new)));
			for (Message.Attachment attachment : message.attachments()) bytesMessage.writeBytes(attachment.data());
		} catch (JMSException e) {
			Logger.error(e.getMessage(), e);
		}
	}
}
