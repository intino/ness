package io.intino.ness.datalake;

import io.intino.ness.datalake.graph.Tank;
import io.intino.ness.inl.Message;
import io.intino.ness.inl.MessageInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static java.time.Instant.parse;
import static java.util.stream.Collectors.toList;
import static org.slf4j.Logger.ROOT_LOGGER_NAME;

public class ReflowMessageInputStream implements MessageInputStream {
	private static Logger logger = LoggerFactory.getLogger(ROOT_LOGGER_NAME);

	private final List<TankInputStream> streams;

	public ReflowMessageInputStream(Map<Tank, Instant> tanks) {
		this.streams = tanks.keySet().stream().map(t -> new TankInputStream(t.qualifiedName(), t.sortedMessagesIterator(tanks.get(t)))).collect(toList());
	}

	@Override
	public String name() {
		return "reflow";
	}

	@Override
	public void name(String value) {
	}

	@Override
	public Message next() {
		if (!hasNext()) return null;
		TankInputStream manager = managerWithOldestMessage(streams);
		final Message message = manager.message;
		manager.next();
		return message;
	}

	@Override
	public boolean hasNext() {
		return flowsAreActive(streams);
	}


	private TankInputStream managerWithOldestMessage(List<TankInputStream> managers) {
		Instant reference = instantOf(managers.get(0).message);
		TankInputStream manager = managers.get(0);
		for (int i = 1; i < managers.size(); i++) {
			Instant comparable = instantOf(managers.get(i).message);
			if (comparable.isBefore(reference)) {
				reference = comparable;
				manager = managers.get(i);
			}
		}
		return manager;
	}

	@Override
	public void close() {
	}

	private Instant instantOf(Message message) {
		return message != null ? parse(message.get("ts")) : Instant.MAX;
	}

	static class TankInputStream {
		final String source;
		private final Iterator<Message> iterator;
		private Message message;

		TankInputStream(String source, Iterator<Message> iterator) {
			this.source = source;
			this.iterator = iterator;
			next();
		}

		private void next() {
			this.message = iterator.next();
		}
	}

	private boolean flowsAreActive(List<TankInputStream> managers) {
		for (TankInputStream manager : managers)
			if (manager.message != null) return true;
		return false;
	}


}