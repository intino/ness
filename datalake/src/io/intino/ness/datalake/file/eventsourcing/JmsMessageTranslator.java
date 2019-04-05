package io.intino.ness.datalake.file.eventsourcing;


import io.intino.alexandria.inl.Message;
import io.intino.alexandria.inl.Message.Attachment;
import io.intino.alexandria.jms.MessageFactory;
import io.intino.alexandria.logger.Logger;
import io.intino.alexandria.zim.ZimReader;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.TextMessage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toMap;

public class JmsMessageTranslator {

	private static String ATTACHMENT_IDS = "__attachment-ids__";
	private static String ATTACHMENT_SIZES = "__attachment-sizes__";
	private static String MESSAGE = "__text-message__";

	public static Message toInlMessage(javax.jms.Message message) {
		try {
			if (message instanceof BytesMessage) {
				final Message result = new ZimReader(message.getStringProperty(MESSAGE)).next();
				Map<String, Integer> attachments = loadAttachmentProperties(message);
				for (String id : attachments.keySet()) {
					byte[] array = new byte[attachments.get(id)];
					((BytesMessage) message).readBytes(array, attachments.get(id));
					result.attachment(id).data(array);
				}
				return result;
			} else return new ZimReader(((TextMessage) message).getText()).next();
		} catch (Throwable e) {
			Logger.error(e);
			return null;
		}
	}

	public static javax.jms.Message fromInlMessage(Message message) {
		List<Attachment> attachments = getAttachments(message);
		if (!attachments.isEmpty()) {
			javax.jms.Message result = MessageFactory.byteMessage();
			addAttachments((BytesMessage) result, attachments);
			addTextMessage(message, (BytesMessage) result);
			return result;
		} else return MessageFactory.createMessageFor(message.toString());
	}

	private static List<Attachment> getAttachments(Message message) {
		List<Attachment> attachments = new ArrayList<>(message.attachments());
		message.components().stream().map(JmsMessageTranslator::getAttachments).forEach(attachments::addAll);
		return attachments;
	}

	private static Map<String, Integer> loadAttachmentProperties(javax.jms.Message message) {
		try {
			final String[] ids = message.getStringProperty(ATTACHMENT_IDS).split(",");
			final String[] sizes = message.getStringProperty(ATTACHMENT_SIZES).split(",");
			return IntStream.range(0, ids.length).boxed().collect(toMap(i -> ids[i], i -> Integer.parseInt(sizes[i])));
		} catch (JMSException e) {
			Logger.error(e);
			return Collections.emptyMap();
		}
	}

	private static void addTextMessage(Message message, BytesMessage result) {
		try {
			result.setStringProperty(MESSAGE, message.toString());
		} catch (JMSException e) {
			Logger.error(e);
		}
	}

	private static void addAttachments(BytesMessage bytesMessage, List<Attachment> attachments) {
		try {
			bytesMessage.setStringProperty(ATTACHMENT_IDS, String.join(",", attachments.stream().map(Attachment::id).toArray(String[]::new)));
			bytesMessage.setStringProperty(ATTACHMENT_SIZES, String.join(",", attachments.stream().map(a -> a.data().length + "").toArray(String[]::new)));
			for (Attachment attachment : attachments) bytesMessage.writeBytes(attachment.data());
		} catch (JMSException e) {
			Logger.error(e);
		}
	}
}
