package io.intino.ness.konos;

import io.intino.tara.magritte.Graph;

public class Main {

	public static void main(String[] args) {
		ApplicationConfiguration configuration = createConfigurationFromArgs(args);
		Graph graph = Setup.initGraph(configuration);
		ApplicationBox box = run(graph, configuration);
		Runtime.getRuntime().addShutdownHook(new Thread(() -> box.quit()));
	}

	private static ApplicationConfiguration createConfigurationFromArgs(String[] args) {
		return new ApplicationConfiguration(args);
	}

	private static ApplicationBox run(Graph graph, ApplicationConfiguration configuration) {
		ApplicationBox box = new ApplicationBox(graph, configuration);
		Setup.configureBox(box);
		box.init();
		return box;
	}
}