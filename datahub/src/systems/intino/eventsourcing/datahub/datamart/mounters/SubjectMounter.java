package systems.intino.eventsourcing.datahub.datamart.mounters;

import io.intino.alexandria.logger.Logger;
import io.intino.magritte.framework.Layer;
import systems.intino.datamarts.subjectstore.SubjectStore;
import systems.intino.datamarts.subjectstore.SubjectStore.Transaction;
import systems.intino.eventsourcing.datahub.datamart.MasterDatamart;
import systems.intino.eventsourcing.datahub.model.Data;
import systems.intino.eventsourcing.datahub.model.Datalake;
import systems.intino.eventsourcing.event.message.MessageEvent;
import systems.intino.eventsourcing.message.Message;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;

public class SubjectMounter extends DatamartMounter {
	public static final String ID = "subject";
	public static final String TS = "ts";
	public static final String SS = "ss";
	private final String subject;
	protected final Set<String> attributes;
	protected final Set<String> numericAttributes;

	public SubjectMounter(MasterDatamart datamart, String subject, Collection<Datalake.Tank.Message> tanks) {
		super(datamart);
		this.subject = subject;
		this.attributes = tanks.stream().flatMap(t -> t.message().attributeList().stream()).map(Layer::name$).collect(Collectors.toSet());
		this.numericAttributes = tanks.stream().flatMap(t -> t.message().attributeList().stream()).filter(Batch::isNumber).map(Layer::name$).collect(Collectors.toSet());
	}

	@Override
	public void mount(MessageEvent event) {
		synchronized (datamart) {
			mount(event.toMessage());
		}
	}

	public String subject() {
		return subject;
	}

	@Override
	public void mount(Message message) {
		if (message == null) return;
		Message.Value id = message.get(ID);
		if (id.isNull() || id.isEmpty()) return;
		try (SubjectStore subjectStore = datamart.subjectsStore().getOrCreate(id.asString())) {
			Transaction transaction = subjectStore.on(message.get(TS).asInstant(), message.get(SS).asString());
			message.attributes().stream()
					.filter(a -> attributes.contains(a))
					.forEach(a -> feed(transaction, a, message.get(a)));
			transaction.commit();
		}
	}

	private void feed(Transaction feed, String attr, Message.Value value) {
		if (numericAttributes.contains(attr)) feed.put(attr, value.asDouble());
		else feed.put(attr, value.asString());
	}

	@Override
	public List<String> destinationsOf(Message message) {
		return emptyList();
	}

	public static class Batch extends SubjectMounter implements AutoCloseable {
		private static final Map<String, SubjectStore> stores = new HashMap<>();
		private static final Map<String, SubjectStore.Batch> batches = new HashMap<>();

		public Batch(MasterDatamart datamart, String subject, Collection<Datalake.Tank.Message> subjectTanks) {
			super(datamart, subject, subjectTanks);
		}

		private static boolean isNumber(Data attribute) {
			return attribute.isReal() || attribute.isDateTime() || attribute.isDate() || attribute.isBool() || attribute.isInteger() || attribute.isLongInteger();
		}

		public void mount(MessageEvent event) {
			Message message = event.toMessage();
			Message.Value id = message.get(ID);
			if (id.isNull() || id.isEmpty()) return;
			SubjectStore.Batch subjectStore = subjectStore(id.asString());
			Transaction feed = subjectStore.on(event.ts(), event.ss());
			message.attributes().stream()
					.filter(attributes::contains)
					.forEach(a -> getFeed(a, feed, message.get(a)));
			feed.commit();
		}

		private void getFeed(String attr, Transaction feed, Message.Value value) {
			if (numericAttributes.contains(attr)) feed.put(attr, value.asDouble());
			else feed.put(attr, value.asString());
		}

		private SubjectStore.Batch subjectStore(String id) {
			if (batches.containsKey(id)) return batches.get(id);
			var store = datamart.subjectsStore().getOrCreateSession(id);
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
					Logger.error(subject() + ": " + e.getMessage());
				}
			});
			stores.values().forEach(SubjectStore::close);
			stores.keySet().forEach(id -> datamart.subjectsStore().commit(id));
			batches.clear();
			stores.clear();
		}

	}
}