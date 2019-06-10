package io.intino.ness.ingestion;

import io.intino.alexandria.Timetag;
import io.intino.alexandria.inl.Message;
import io.intino.alexandria.inl.MessageWriter;
import io.intino.alexandria.logger.Logger;
import io.intino.ness.Fingerprint;
import io.intino.ness.Session;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import java.util.zip.GZIPOutputStream;

public class EventSession {
	private final Map<Fingerprint, MessageWriter> writers = new HashMap<>();
	private SessionHandler.Provider provider;

	public EventSession(SessionHandler.Provider provider) {
		this.provider = provider;
	}

	public void put(String tank, Timetag timetag, Message... messages) {
		put(tank, timetag, Arrays.stream(messages));
	}

	public void put(String tank, Timetag timetag, Stream<Message> messages) {
		put(writerOf(tank, timetag), messages);
	}

	public void flush() {
		try {
			for (MessageWriter w : writers.values()) w.flush();
		} catch (IOException e) {
			Logger.error(e);
		}
	}

	public void close() {
		try {
			for (MessageWriter w : writers.values()) w.close();
		} catch (IOException e) {
			Logger.error(e);
		}
	}

	private void put(MessageWriter writer, Stream<Message> messages) {
		messages.forEach(m -> write(writer, m));
	}

	private void write(MessageWriter writer, Message message) {
		try {
			writer.write(message);
		} catch (IOException e) {
			Logger.error(e);
		}
	}

	private MessageWriter writerOf(String tank, Timetag timetag) {
		return writerOf(Fingerprint.of(tank, timetag));
	}

	private MessageWriter writerOf(Fingerprint fingerprint) {
		if (!writers.containsKey(fingerprint)) writers.put(fingerprint, createWriter(fingerprint));
		return writers.get(fingerprint);
	}

	private MessageWriter createWriter(Fingerprint fingerprint) {
		return new MessageWriter(zipStream(provider.outputStream(fingerprint.name(), Session.Type.event)));
	}

	private GZIPOutputStream zipStream(OutputStream outputStream) {
		try {
			return new GZIPOutputStream(outputStream);
		} catch (IOException e) {
			Logger.error(e);
			return null;
		}
	}

}
