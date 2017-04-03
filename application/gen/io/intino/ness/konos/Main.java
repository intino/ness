package io.intino.ness.konos;

import io.intino.tara.magritte.Graph;

public class Main {

	public static void main(String[] args) {
		NessConfiguration configuration = createConfigurationFromArgs(args);
		Graph graph = Setup.initGraph(configuration);
		NessBox box = run(graph, configuration);
		Runtime.getRuntime().addShutdownHook(new Thread(() -> box.quit()));
	}

	private static NessConfiguration createConfigurationFromArgs(String[] args) {
		return new NessConfiguration(args);
	}

	private static NessBox run(Graph graph, NessConfiguration configuration) {
		NessBox box = new NessBox(graph, configuration);
		Setup.configureBox(box);
		box.init();
		Setup.execute(box);
		return box;
	}
}