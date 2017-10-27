package io.intino.ness.box;

import io.intino.konos.Box;
import io.intino.ness.datalake.FunctionHelper;
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
		Graph graph = new Graph(store(boxConfiguration.args().get("ness_store"))).loadStashes("Ness");
		compileFunctions(graph);
		Box box = new NessBox(boxConfiguration).put(graph).open();
		Runtime.getRuntime().addShutdownHook(new Thread(box::close));
	}

	private static void compileFunctions(Graph graph) {
		for (Function function : graph.as(NessGraph.class).functionList())
			function.aClass(FunctionHelper.compile(function.qualifiedName(), function.source()));
		logger.info("Compiled functions");
	}

	private static io.intino.tara.magritte.Store store(String directory) {
		return new InMemoryFileStore(new File(directory)) {
			public void writeStash(Stash stash, String path) {
				stash.language = stash.language == null || stash.language.isEmpty() ? "Ness" : stash.language;
				super.writeStash(stash, path);
			}
		};
	}
}