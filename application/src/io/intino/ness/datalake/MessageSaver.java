package io.intino.ness.datalake;

import io.intino.ness.inl.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.List;

import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;

class MessageSaver {

	private static Logger logger = LoggerFactory.getLogger(MessageSaver.class);

	static void append(File inlFile, Message message, String textMessage) {
		write(inlFile, textMessage, CREATE, APPEND);
		saveAttachments(inlFile.getParentFile(), message);
	}

	static void save(File inlFile, List<Message> messages) {
		StringBuilder builder = new StringBuilder();
		for (Message m : messages) builder.append(m.toString()).append("\n\n");
		write(inlFile, builder.toString(), CREATE);
	}

	static synchronized void write(File inlFile, String textMessage, StandardOpenOption... option) {
		try {
			Files.write(inlFile.toPath(), (textMessage + "\n\n").getBytes(), option);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
	}

	private static void saveAttachments(File directory, Message message) {
//		for (Attachment attachment : message.attachments()) {
//			Files.write(new File(directory, attachment.name()).toPath(), attachment.asByteArray());
//		}
	}
}
