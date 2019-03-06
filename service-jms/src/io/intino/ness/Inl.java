package io.intino.ness;

import io.intino.alexandria.inl.Message;
import io.intino.alexandria.logger.Logger;
import io.intino.alexandria.zim.ZimReader;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Inl {

	public static List<Message> load(String text) {
		ZimReader zimReader = new ZimReader(text);
		List<Message> messages = new ArrayList<>();
		while (zimReader.hasNext()) messages.add(zimReader.next());
		return messages;
	}

	public static List<Message> load(File inlFile) {
		try {
			return load(new String(readFile(inlFile), Charset.forName("UTF-8")));
		} catch (IOException e) {
			Logger.error(e.getMessage(), e);
			return Collections.emptyList();
		}
	}

	private static byte[] readFile(File inlFile) throws IOException {
		return Files.readAllBytes(inlFile.toPath());
	}
}
