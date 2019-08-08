package org.example.datahubtest.box;

import io.intino.alexandria.core.Box;

public class Main {
	public static void main(String[] args) {
		Box box = new DataHubTestBox(args).put(new io.intino.tara.magritte.Graph().loadStashes("solution")).open();
		Runtime.getRuntime().addShutdownHook(new Thread(box::close));
	}
}