package systems.intino.eventsourcing.datahub.datamart.impl;

import io.intino.alexandria.logger.Logger;
import org.apache.commons.io.FileUtils;
import systems.intino.datamarts.subjectstore.SubjectStore;
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

public class SubjectsDirectory {
	private final Set<String> subscribedEvents;
	private final File root;

	public SubjectsDirectory(Datamart definition, File root) {
		this.root = root;
		subscribedEvents = types(definition.subjectList().stream()
				.flatMap(s -> s.from().stream()))
				.collect(Collectors.toSet());
		boolean mkdirs = this.root.mkdirs();
	}

	protected String extension() {
		return DatahubBox.SUBJECT_EXTENSION;
	}

	public SubjectStore get(String id) {
		return contains(id) ? new SubjectStore(id, fileOf(id)) : null;
	}

	public SubjectStore getOrCreate(String id) {
		return new SubjectStore(id, fileOf(id));
	}

	public SubjectStore getOrCreateSession(String id) {
		return new SubjectStore(id, sessionFileOf(id));
	}

	private File sessionFileOf(String id) {
		root.mkdirs();
		return new File(root, normalizePath(id + extension() + ".session"));
	}

	public void commit(String id) {
		sessionFileOf(id).renameTo(fileOf(id));
	}

	public boolean isSubscribedTo(Datalake.Tank tank) {
		return tank.isMessage() && subscribedEvents.contains(tank.asMessage().message().name$());
	}

	public Stream<SubjectStore> stream() {
		return listFiles().stream().map(s -> new SubjectStore(s.getName().replace(extension(), ""), s));
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

	public boolean contains(String id) {
		return fileOf(id).exists();
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
