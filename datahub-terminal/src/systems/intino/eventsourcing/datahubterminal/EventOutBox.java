package systems.intino.eventsourcing.datahubterminal;

import io.intino.alexandria.Scale;
import io.intino.alexandria.Timetag;
import io.intino.alexandria.logger.Logger;
import systems.intino.eventsourcing.event.Event;
import systems.intino.eventsourcing.event.message.MessageEvent;
import systems.intino.eventsourcing.message.MessageReader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

class EventOutBox extends OutBox {
	private static final String INL = ".inl";

	EventOutBox(File directory) {
		super(directory);
	}

	List<Map.Entry<String, Event>> get() {
		List<Map.Entry<String, Event>> events = new ArrayList<>();
		synchronized (files) {
			files.sort(Comparator.comparingLong(File::lastModified));
			if (files.isEmpty()) return Collections.emptyList();
			for (File file : files) {
				try {
					if (!file.exists()) {
						files.remove(file);
						continue;
					}
					String content = Files.readString(file.toPath());
					if (content.isBlank()) {
						file.delete();
						files.remove(file);
						continue;
					}
					MessageReader messages = new MessageReader(content);
					events.add(new AbstractMap.SimpleEntry<>(destination(file), new MessageEvent(messages.next())));
					messages.close();
				} catch (Exception e) {
					Logger.error(e);
				}
			}
		}
		return events;
	}

	void push(String channel, Event event) {
		File file = new File(directory, channel + "#" + timetag(event) + "#" + UUID.randomUUID() + INL);
		try {
			Files.write(file.toPath(), event.toString().getBytes());
		} catch (IOException e) {
			Logger.error(e);
		}
		synchronized (files) {
			files.add(file);
		}
	}

	private String timetag(Event event) {
		return new Timetag(LocalDateTime.ofInstant(event.ts(), ZoneOffset.UTC), Scale.Minute).toString();
	}

	protected String destination(File file) {
		return file.getName().substring(0, file.getName().indexOf("#"));
	}

	@Override
	protected String extension() {
		return INL;
	}
}