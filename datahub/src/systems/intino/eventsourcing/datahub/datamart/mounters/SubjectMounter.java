package systems.intino.eventsourcing.datahub.datamart.mounters;

import io.intino.alexandria.logger.Logger;
import io.intino.magritte.framework.Layer;
import systems.intino.alexandria.datamarts.SubjectStore;
import systems.intino.alexandria.datamarts.SubjectStore.Transaction;
import systems.intino.eventsourcing.datahub.datamart.MasterDatamart;
import systems.intino.eventsourcing.datahub.model.Data;
import systems.intino.eventsourcing.datahub.model.Datalake;
import systems.intino.eventsourcing.event.message.MessageEvent;
import systems.intino.eventsourcing.message.Message;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;

public class SubjectMounter extends DatamartMounter {
	public static final String ID = "subject";
	public static final String TS = "ts";
	public static final String SS = "ss";
	protected final Set<String> attributes;
	protected final Set<String> numericAttributes;

	public SubjectMounter(MasterDatamart datamart, Set<Datalake.Tank.Message> tanks) {
		super(datamart);
		this.attributes = tanks.stream().flatMap(t -> t.message().attributeList().stream()).map(Layer::name$).collect(Collectors.toSet());
		this.numericAttributes = tanks.stream().flatMap(t -> t.message().attributeList().stream()).filter(Batch::isNumber).map(Layer::name$).collect(Collectors.toSet());
	}

	@Override
	public void mount(MessageEvent event) {
		synchronized (datamart) {
			mount(event.toMessage());
		}
	}

	@Override
	public void mount(Message message) {
		if (message == null) return;
		Message.Value id = message.get(ID);
		if (id.isNull() || id.isEmpty()) return;
		try (SubjectStore subjectStore = datamart.subjectsStore().get(id.asString())) {
			Transaction feed = subjectStore.feed(message.get(TS).asInstant(), message.get(SS).asString());
			message.attributes().forEach(a -> getFeed(a, feed, message.get(a)));
			feed.terminate();
		}
	}

	private void getFeed(String attr, SubjectStore.Transaction feed, Message.Value value) {
		if (numericAttributes.contains(attr)) feed.add(attr, value.asDouble());
		else feed.add(attr, value.asString());
	}

	@Override
	public List<String> destinationsOf(Message message) {
		return emptyList();
	}

	public static class Batch extends SubjectMounter implements AutoCloseable {
		private final Map<String, SubjectStore> stores;
		private final Map<String, SubjectStore.Batch> batches;

		public Batch(MasterDatamart datamart, Set<Datalake.Tank.Message> subjectTanks) {
			super(datamart, subjectTanks);
			this.stores = new HashMap<>();
			this.batches = new HashMap<>();
		}

		private static boolean isNumber(Data attribute) {
			return attribute.isReal() || attribute.isDateTime() || attribute.isDate() || attribute.isBool() || attribute.isInteger() || attribute.isLongInteger();
		}

		public void mount(MessageEvent event) {
			Message message = event.toMessage();
			SubjectStore.Batch subjectStore = subjectStore(message.get(ID).asString());
			Transaction feed = subjectStore.feed(event.ts(), event.ss());
			message.attributes().stream()
					.filter(attributes::contains)
					.forEach(a -> getFeed(a, feed, message.get(a)));
			feed.terminate();
		}

		private void getFeed(String attr, SubjectStore.Transaction feed, Message.Value value) {
			if (numericAttributes.contains(attr)) feed.add(attr, value.asDouble());
			else feed.add(attr, value.asString());
		}

		private SubjectStore.Batch subjectStore(String id) {
			if (batches.containsKey(id)) return batches.get(id);
			var store = datamart.subjectsStore().getSession(id);
			SubjectStore.Batch batch = store.batch();
			stores.put(id, store);
			batches.put(id, batch);
			return batch;
		}

		@Override
		public void close() throws Exception {
			batches.values().forEach(b -> {
				try {
					b.terminate();
				} catch (Exception e) {
					Logger.error(e);
				}
			});
			stores.values().forEach(SubjectStore::close);
			stores.keySet().forEach(id -> datamart.subjectsStore().commit(id));
		}

	}
}