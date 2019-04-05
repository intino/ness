package io.intino.ness.ingestion;

import io.intino.alexandria.Timetag;
import io.intino.alexandria.inl.Message;
import io.intino.alexandria.logger.Logger;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import java.util.zip.GZIPOutputStream;

public class EventSession {
	private final Map<Fingerprint, BufferedWriter> writers = new HashMap<>();
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
			for (BufferedWriter w : writers.values()) w.flush();
		} catch (IOException e) {
			Logger.error(e);
		}
	}

	public void close() {
		try {
			for (BufferedWriter w : writers.values()) w.close();
		} catch (IOException e) {
			Logger.error(e);
		}
	}

	private void put(BufferedWriter writer, Stream<Message> messages) {
		messages.forEach(m -> write(writer, m));
	}

	private void write(BufferedWriter writer, Message message) {
		try {
			writer.write(message.toString() + "\n\n");
		} catch (IOException e) {
			Logger.error(e);
		}
	}

	private BufferedWriter writerOf(String tank, Timetag timetag) {
		return writerOf(Fingerprint.of(tank, timetag));
	}

	private BufferedWriter writerOf(Fingerprint fingerprint) {
		if (!writers.containsKey(fingerprint)) writers.put(fingerprint, createWriter(fingerprint));
		return writers.get(fingerprint);
	}

	private BufferedWriter createWriter(Fingerprint fingerprint) {
		return new BufferedWriter(new OutputStreamWriter(zipStream(provider.outputStream(fingerprint.name(), Session.Type.event))));
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
