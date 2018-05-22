package io.intino.ness.datalake;

import io.intino.ness.inl.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static io.intino.ness.datalake.graph.Tank.INL;

public class AttachmentLoader {

	private static Logger logger = LoggerFactory.getLogger(AttachmentLoader.class);

	public static Message loadAttachments(File inlFile, Message message) {
		final File directory = attachmentDirectoryOf(inlFile);
		try {
			for (Message.Attachment attachment : message.attachments())
				attachment.data(Files.readAllBytes(new File(directory, attachment.id()).toPath()));
			return message;
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
		return message;
	}

	private static File attachmentDirectoryOf(File file) {
		return new File(file.getParentFile(), file.getName().replace(INL, ""));
	}

}
