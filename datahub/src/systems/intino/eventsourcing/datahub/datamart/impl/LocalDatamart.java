package systems.intino.eventsourcing.datahub.datamart.impl;

import systems.intino.eventsourcing.datahub.box.DatahubBox;
import systems.intino.eventsourcing.datahub.datamart.DatamartUtils;
import systems.intino.eventsourcing.datahub.datamart.MasterDatamart;
import systems.intino.eventsourcing.datahub.datamart.mounters.DatamartMounter;
import systems.intino.eventsourcing.datahub.datamart.mounters.SubjectMounter;
import systems.intino.eventsourcing.datahub.model.Datalake;
import systems.intino.eventsourcing.datahub.model.Datamart;
import systems.intino.eventsourcing.datahub.model.Subject;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class LocalDatamart implements MasterDatamart {
	private final DatahubBox box;
	private final Datamart definition;
	private final File directory;
	private final SubjectsStore subjects;
	private final Map<Datalake.Tank, List<SubjectMounter>> subjectMounters;

	public LocalDatamart(DatahubBox box, Datamart definition) {
		this.box = box;
		this.definition = definition;
		this.directory = box.datamartDirectory(definition.name$());
		this.subjects = new SubjectsStore(definition, new File(directory, "subjects"));
		this.subjectMounters = calculateSubjectMounters();
	}

	@Override
	public Datamart definition() {
		return definition;
	}

	public File directory() {
		return directory;
	}

	@Override
	public DatahubBox box() {
		return box;
	}

	@Override
	public String name() {
		return definition.name$();
	}

	@Override
	public SubjectsStore subjectsStore() {
		return subjects;
	}

	@Override
	public List<? extends DatamartMounter> mountersFor(Datalake.Tank tank) {
		return !tank.isMessage() ?
				List.of() :
				subjectMounters.getOrDefault(tank, List.of());
	}

	@Override
	public void close() {
	}

	private Map<Datalake.Tank, List<SubjectMounter>> calculateSubjectMounters() {
		Map<Datalake.Tank, List<SubjectMounter>> tankMounters = new HashMap<>();
		Map<Subject, Set<Datalake.Tank.Message>> subjectTanks = definition.subjectList().stream()
				.collect(Collectors.toMap(subject -> subject, subject -> DatamartUtils.subjectTanks(definition, subject), (a, b) -> b));
		for (Datalake.Tank tank : box.graph().datalake().tankList())
			tankMounters.put(tank, subjectTanks.keySet().stream()
					.filter(subject -> subjectTanks.get(subject).contains(tank.asMessage()))
					.map(subject -> new SubjectMounter(this, subject.name$(), subjectTanks.get(subject))).
					collect(Collectors.toList()));
		return tankMounters;
	}
}