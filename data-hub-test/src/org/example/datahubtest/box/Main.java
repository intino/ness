package org.example.datahubtest.box;

import io.intino.alexandria.core.Box;

public class Main {
	public static void main(String[] args) {
		Box box = new DataHubTestBox(args);
		io.intino.tara.magritte.Graph graph = new io.intino.tara.magritte.Graph().loadStashes("DataHubTest");
		box.put(graph);
		box.open();
		Runtime.getRuntime().addShutdownHook(new Thread(box::close));
	}
}