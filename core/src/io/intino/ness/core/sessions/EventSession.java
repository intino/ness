package io.intino.ness.core.sessions;

import io.intino.alexandria.Timetag;
import io.intino.alexandria.inl.Message;
import io.intino.alexandria.logger.Logger;
import io.intino.ness.core.Stage;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import java.util.zip.GZIPOutputStream;

import static io.intino.ness.core.Blob.Type.event;

public class EventSession {
	private final Map<Fingerprint, BufferedWriter> writers = new HashMap<>();
	private io.intino.ness.core.Stage stage;

	public EventSession(Stage stage) {
		this.stage = stage;
	}

	public void put(String tank, Timetag timetag, Stream<Message> messages) {
		put(writerOf(tank, timetag), messages);
	}

	private void put(BufferedWriter writer, Stream<Message> messages) {
		messages.forEach(m -> write(writer, m));
	}

	private BufferedWriter writerOf(String tank, Timetag timetag) {
		return writerOf(Fingerprint.of(tank, timetag));
	}

	private BufferedWriter writerOf(Fingerprint fingerprint) {
		if (!writers.containsKey(fingerprint)) writers.put(fingerprint, createWriter(fingerprint));
		return writers.get(fingerprint);
	}

	private BufferedWriter createWriter(Fingerprint fingerprint) {
		return new BufferedWriter(new OutputStreamWriter(zipStream(stage.start(fingerprint.name(), event))));
	}

	private GZIPOutputStream zipStream(OutputStream outputStream) {
		try {
			return new GZIPOutputStream(outputStream);
		} catch (IOException e) {
			Logger.error(e);
			return null;
		}
	}

	public void put(String tank, Timetag timetag, Message... messages) {
		put(tank, timetag, Arrays.stream(messages));
	}

	public void close() {
		try {
			for (BufferedWriter w : writers.values()) w.close();
		} catch (IOException e) {
			Logger.error(e);
		}
	}

	private void write(BufferedWriter writer, Message message) {
		try {
			writer.write(message.toString() + "\n\n");
		} catch (IOException e) {
			Logger.error(e);
		}
	}

}
