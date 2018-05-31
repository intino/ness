package io.intino.ness.box;

import io.intino.ness.datalake.FunctionHelper;
import io.intino.ness.datalake.graph.DatalakeGraph;
import io.intino.ness.graph.Function;
import io.intino.ness.graph.NessGraph;
import io.intino.tara.io.Stash;
import io.intino.tara.magritte.Graph;
import io.intino.tara.magritte.stores.InMemoryFileStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class Main {
	private static Logger logger = LoggerFactory.getLogger(Main.class);

	public static void main(String[] args) {
		NessConfiguration boxConfiguration = new NessConfiguration(args);
		final NessBox box = new NessBox(boxConfiguration);
		Graph graph = new Graph(store(box.storeDirectory())).loadStashes("Ness");
		if (box.configuration.args().containsKey("configurationModel") && !box.configuration.args().get("configurationModel").isEmpty())
			graph.loadStashes(box.configuration.args().get("configurationModel"));
		final DatalakeGraph datalakeGraph = new Graph(store(box.storeDirectory())).loadStashes("Datalake").as(DatalakeGraph.class);
		box.put(graph.as(NessGraph.class)).put(datalakeGraph);
		graph.as(NessGraph.class).tankList().forEach(t -> datalakeGraph.add(t.qualifiedName()));
		datalakeGraph.core$().save("tanks");

		compileFunctions(graph);
		box.open();
		Runtime.getRuntime().addShutdownHook(new Thread(box::close));
	}

	private static void compileFunctions(Graph graph) {
		for (Function function : graph.as(NessGraph.class).functionList())
			function.aClass(FunctionHelper.compile(function.qualifiedName(), function.source()));
		logger.info("Compiled functions");
	}

	private static io.intino.tara.magritte.Store store(String directory) {
		return new InMemoryFileStore(new File(directory)){
			@Override
			public void writeStash(Stash stash, String path) {
				stash.language = "Ness";
				super.writeStash(stash, path);
			}
		};
	}
}