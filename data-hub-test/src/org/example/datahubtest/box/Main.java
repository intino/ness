package org.example.datahubtest.box;

import io.intino.alexandria.core.Box;

public class Main {
	public static void main(String[] args) {
        Box box = new DataHubTestBox(args).put(new io.intino.magritte.framework.Graph().loadStashes("solution")).start();
        Runtime.getRuntime().addShutdownHook(new Thread(box::stop));
    }
}