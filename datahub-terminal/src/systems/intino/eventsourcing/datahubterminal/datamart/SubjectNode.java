package systems.intino.eventsourcing.datahubterminal.datamart;

import io.intino.alexandria.logger.Logger;
import systems.intino.alexandria.datamarts.SubjectStore;

import java.io.Closeable;
import java.io.File;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@SuppressWarnings("resource")
public class SubjectNode implements Closeable {
	private final Datamart datamart;
	private final String id;
	private final String type;
	private final DatamartAccessor accessor;
	private final File file;
	private final List<SubjectListener<?>> listeners;
	private SubjectStore store;

	public SubjectNode(Datamart datamart, String id, String type, DatamartAccessor accessor) {
		this.datamart = datamart;
		this.id = id;
		this.type = type;
		this.file = null;
		this.accessor = accessor;
		this.listeners = new CopyOnWriteArrayList<>();
	}

	public SubjectNode(Datamart datamart, String id, String type, File file) {
		this.datamart = datamart;
		this.id = id;
		this.type = type;
		this.file = file;
		this.accessor = null;
		this.listeners = new CopyOnWriteArrayList<>();
	}

	public String id() {
		return id;
	}

	public String type() {
		return type;
	}

	public boolean enabled() {
		var store = store();
		return store != null && store.currentText("enabled").equals("true");
	}

	public Double currentNumber(String tag) {
		return store.currentNumber(tag);
	}

	public String currentText(String tag) {
		return store.currentText(tag);
	}

	public void update(Instant ts, String ss, Map<String, Object> values) {
		SubjectStore.Transaction feed = store.feed(ts, ss);
		values.forEach((k, v) -> {
			switch (v) {
				case Number n -> feed.add(k, (double) n);
				case String s -> feed.add(k, s);
				case Boolean b -> feed.add(k, !b ? 0 : 1);
				case Instant i -> feed.add(k, i.toEpochMilli());
				case List<?> l -> feed.add(k, l.stream().map(Object::toString).collect(Collectors.joining("\0")));
				default -> feed.add(k, v.toString());
			}
		});
		feed.terminate();
	}

	public Datamart datamart() {
		return datamart;
	}

	public void addChangeListener(SubjectListener<?> listener) {
		this.listeners.add(listener);
	}

	private SubjectStore store() {
		if (this.store != null) return this.store;
		if (this.file != null) return new SubjectStore(this.file, id);
		this.store = downloadStore();
		return this.store;
	}


	@Override
	public void close() {
		if (store != null) store.close();
	}


	private SubjectStore downloadStore() {
		try {
			return accessor == null ? null : accessor.getSubject(id);
		} catch (Exception e) {
			Logger.error(e);
			return null;
		}
	}
}
