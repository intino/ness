package systems.intino.eventsourcing.datahub.datamart.impl;

import io.intino.alexandria.logger.Logger;
import org.apache.commons.io.FileUtils;
import systems.intino.datamarts.subjectstore.SubjectStore;
import systems.intino.datamarts.subjectstore.model.Subject;
import systems.intino.eventsourcing.datahub.box.DatahubBox;
import systems.intino.eventsourcing.datahub.model.Datalake;
import systems.intino.eventsourcing.datahub.model.Datamart;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static systems.intino.eventsourcing.datahub.datamart.DatamartUtils.types;
import static systems.intino.eventsourcing.datahub.datamart.MasterDatamart.normalizePath;

public class SubjectsStore {
	private final Set<String> subscribedEvents;
	private final File root;
	private final SubjectStore store;
	private final String source;

	public SubjectsStore(Datamart definition, File root) {
		this.root = root;
		this.source = "jdbc:sqlite:" + normalizePath(root.getAbsolutePath()) + "/subjects" + DatahubBox.SUBJECT_EXTENSION;
		subscribedEvents = types(definition.subjectList().stream()
				.flatMap(s -> s.from().stream()))
				.collect(Collectors.toSet());
		boolean mkdirs = this.root.mkdirs();
		store = new SubjectStore(source);
	}

	public String source() {
		return source;
	}

	protected String extension() {
		return DatahubBox.SUBJECT_EXTENSION;
	}

	public Subject get(String name, String type) {
		return store.get(name, type);
	}

	public Subject getOrCreate(String name, String type) {
		try {
			Subject subject = store.get(name, type);
			return subject != null ? subject : store.create(name, type);
		} catch (Throwable e) {
			Logger.error(e);
			return null;
		}
	}

	public boolean isSubscribedTo(Datalake.Tank tank) {
		return tank.isMessage() && subscribedEvents.contains(tank.asMessage().message().name$());
	}

	public Stream<Subject> stream() {
		return store.subjects().collect().stream();
	}

	public void clear() {
		for (File file : listFiles()) {
			try {
				file.delete();
			} catch (Exception e) {
				Logger.error(e);
			}
		}
	}

	private File fileOf(String id) {
		root.mkdirs();
		return new File(root, normalizePath(id + extension()));
	}

	private List<File> listFiles() {
		return root.exists()
				? new ArrayList<>(FileUtils.listFiles(root, new String[]{extension(), extension().substring(1)}, true))
				: emptyList();
	}
}
