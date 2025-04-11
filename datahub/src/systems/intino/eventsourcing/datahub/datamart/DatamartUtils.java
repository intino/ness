package systems.intino.eventsourcing.datahub.datamart;

import systems.intino.eventsourcing.datahub.model.Datalake;
import systems.intino.eventsourcing.datahub.model.Datamart;
import systems.intino.eventsourcing.datahub.model.Subject;
import systems.intino.eventsourcing.datalake.Datalake.Store;
import systems.intino.eventsourcing.event.message.MessageEvent;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DatamartUtils {

	public static Set<Datalake.Tank.Message> subjectTanks(MasterDatamart datamart, Subject subject) {
		return subjectTanks(datamart.definition(), subject);
	}

	public static Set<Datalake.Tank.Message> subjectTanks(Datamart definition, Subject subject) {
		Set<Datalake.Tank.Message> tanks = new HashSet<>(subject.from());
		definition.subjectList(s -> isDescendantOf(s, subject)).stream()
				.flatMap(s -> s.from().stream()).forEach(tanks::add);
		return tanks;
	}

	public static List<Store.Tank<MessageEvent>> messageTanksOf(systems.intino.eventsourcing.datalake.Datalake datalake, Datamart dm, Subject subject) {
		List<Store.Tank<MessageEvent>> tanks = subject.from().stream().map(message -> datalake.messageStore().tank(tankName(message))).collect(Collectors.toList());
		dm.subjectList(e -> isDescendantOf(e, subject)).stream()
				.flatMap(s -> s.from().stream())
				.map(tank -> datalake.messageStore().tank(tankName(tank)))
				.forEach(tanks::add);
		return tanks;
	}

	public static boolean isDescendantOf(Subject node, Subject expectedParent) {
		if (!node.isExtensionOf()) return false;
		Subject parent = node.asExtensionOf().subject();
		return parent.equals(expectedParent) || isDescendantOf(parent, expectedParent);
	}

	public static Stream<String> types(Stream<Datalake.Tank.Message> t) {
		return asTypes(t.map(DatamartUtils::tankName));
	}


	private static String tankName(Datalake.Tank.Message e) {
		return e == null ? null : e.message().core$().fullName().replace("$", ".");
	}

	private static String withOutParameters(String ss) {
		return ss.contains("?") ? ss.substring(0, ss.indexOf("?")) : ss;
	}

	private static Map<String, String> parameters(String ss) {
		int i = ss.indexOf("?");
		if (i < 0 || i == ss.length() - 1) return Map.of();
		String[] parameters = ss.substring(i + 1).split(";");
		return Arrays.stream(parameters).map(p -> p.split("=")).collect(Collectors.toMap(p -> p[0], p -> p[1]));
	}


	private static Stream<String> asTypes(Stream<String> tanks) {
		return tanks.map(t -> t.contains(".") ? t.substring(t.lastIndexOf(".") + 1) : t);
	}
}