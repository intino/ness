package io.intino.ness.box;

import io.intino.ness.datalake.FunctionHelper;
import io.intino.ness.graph.Function;
import io.intino.ness.graph.NessGraph;
import io.intino.ness.datalake.graph.Tank;
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
		Graph graph = new Graph(store(box.storeDirectory())).loadStashes("Datalake", "Ness");
		compileFunctions(graph);
		box.put(graph).open();
		Runtime.getRuntime().addShutdownHook(new Thread(box::close));
	}

	private static void compileFunctions(Graph graph) {
		for (Function function : graph.as(NessGraph.class).functionList())
			function.aClass(FunctionHelper.compile(function.qualifiedName(), function.source()));
		logger.info("Compiled functions");
	}

	private static io.intino.tara.magritte.Store store(String directory) {
		return new InMemoryFileStore(new File(directory));
	}
}