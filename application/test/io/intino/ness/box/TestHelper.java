package io.intino.ness.box;

import io.intino.ness.datalake.FunctionHelper;
import io.intino.ness.graph.Function;
import io.intino.ness.graph.NessGraph;
import io.intino.tara.io.Stash;
import io.intino.tara.magritte.stores.InMemoryFileStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class TestHelper {
	private static Logger logger = LoggerFactory.getLogger(Main.class);


	static void compileFunctions(NessGraph graph) {
		for (Function function : graph.functionList())
			function.aClass(FunctionHelper.compile(function.qualifiedName(), function.source()));
		logger.info("Compiled functions");
	}

	 static io.intino.tara.magritte.Store store(String directory) {
		return new InMemoryFileStore(new File(directory)) {
			public void writeStash(Stash stash, String path) {
				stash.language = stash.language == null || stash.language.isEmpty() ? "Ness" : stash.language;
				super.writeStash(stash, path);
			}
		};
	}
}
