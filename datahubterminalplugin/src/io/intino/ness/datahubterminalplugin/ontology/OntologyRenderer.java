package io.intino.ness.datahubterminalplugin.ontology;

import io.intino.Configuration;
import io.intino.alexandria.logger.Logger;
import io.intino.datahub.model.Datalake;
import io.intino.datahub.model.Event;
import io.intino.datahub.model.NessGraph;
import io.intino.datahub.model.Wordbag;
import io.intino.ness.datahubterminalplugin.event.EventRenderer;
import io.intino.ness.datahubterminalplugin.event.WordbagRenderer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

public class OntologyRenderer {
	private final List<Datalake.Tank.Event> eventTanks;
	private final List<Event> events;
	private final List<Wordbag> wordbags;
	private final Configuration conf;
	private final File root;
	private final Map<Event, Datalake.Split> eventSplitMap;
	private final File srcDir;
	private final List<File> resDirectories;
	private final String basePackage;

	OntologyRenderer(NessGraph graph, Configuration conf, File root, File srcDir, List<File> resDirectories, String basePackage) {
		this.eventTanks = eventTanks(graph);
		this.events = graph.eventList();
		this.wordbags = graph.wordbagList();
		this.conf = conf;
		this.root = root;
		this.eventSplitMap = splitEvents();
		this.srcDir = srcDir;
		this.resDirectories = resDirectories;
		this.basePackage = basePackage;
		srcDir.mkdirs();
	}

	public boolean render() {
		eventSplitMap.forEach((k, v) -> new EventRenderer(k, v, srcDir, basePackage).render());
		events.stream().filter(event -> !eventSplitMap.containsKey(event)).parallel().forEach(event -> new EventRenderer(event, null, srcDir, basePackage).render());
		wordbags.stream().parallel().forEach(w -> new WordbagRenderer(w, conf, srcDir, resDirectories, basePackage).render());
		File resDirectory = new File(root, "res");
		resDirectory.mkdirs();
		wordbags.stream().filter(Wordbag::isInResource).map(Wordbag::asInResource).
				forEach(w -> {
					File source = new File(w.tsv().getPath());
					File destination = new File(resDirectory, relativeResource(source));
					destination.getParentFile().mkdirs();
					try {
						if (!destination.exists()) Files.copy(w.tsv().openStream(), destination.toPath());
					} catch (IOException e) {
						Logger.error(e);
					}
				});
		return true;
	}


	private Map<Event, Datalake.Split> splitEvents() {
		Map<Event, Datalake.Split> events = new HashMap<>();
		for (Datalake.Tank.Event tank : eventTanks) {
			List<Event> hierarchy = hierarchy(tank.event());
			Datalake.Split split = tank.asTank().isSplitted() ? tank.asTank().asSplitted().split() : null;
			events.put(hierarchy.get(0), split);
			hierarchy.remove(0);
			hierarchy.forEach(e -> events.put(e, null));
		}
		return events;
	}

	private String relativeResource(File resourceFile) {
		String file = resourceFile.getAbsolutePath();
		for (File resDirectory : resDirectories) file = file.replace(resDirectory.getAbsolutePath(), "");
		return file;
	}

	private List<Datalake.Tank.Event> eventTanks(NessGraph nessGraph) {
		if (nessGraph.datalake() == null) return Collections.emptyList();
		return nessGraph.datalake().tankList().stream().filter(Datalake.Tank::isEvent).map(Datalake.Tank::asEvent).collect(Collectors.toList());
	}


	private List<Event> hierarchy(Event event) {
		Set<Event> events = new LinkedHashSet<>();
		events.add(event);
		if (event.isExtensionOf()) events.addAll(hierarchy(event.asExtensionOf().parent()));
		return new ArrayList<>(events);
	}
}
