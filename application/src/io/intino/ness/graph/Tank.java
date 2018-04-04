package io.intino.ness.graph;

import io.intino.ness.datalake.DatalakeManager;
import io.intino.ness.inl.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.util.List;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;

public class Tank extends AbstractTank {

	private static Logger logger = LoggerFactory.getLogger(Tank.class);
	private File currentFile = null;
	private Writer writer = null;

	public Tank(io.intino.tara.magritte.Node node) {
		super(node);
	}

	private static void saveAttachments(File directory, Message message) {
//		for (Attachment attachment : message.attachments()) {
//			Files.write(new File(directory, attachment.name()).toPath(), attachment.asByteArray());
//		}
	}

	public void overwrite(File file, List<Message> messages) {
		try {
			Files.write(file.toPath(), DatalakeManager.toString(messages).getBytes(), CREATE, TRUNCATE_EXISTING);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
	}

	public void append(File inlFile, Message message, String textMessage) {
		try {
			if (writer == null || !currentFile.equals(inlFile)) {
				if (writer != null) writer.close();
				if (!inlFile.exists()) inlFile.createNewFile();
				currentFile = inlFile;
				writer = new BufferedWriter(new FileWriter(inlFile, true));
			}
			writer.write(textMessage + "\n\n");
			saveAttachments(inlFile.getParentFile(), message);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
	}

	public void terminate() {
		try {
			if (writer != null) writer.close();
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
	}
}