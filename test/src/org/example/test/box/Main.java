package org.example.test.box;

import io.intino.alexandria.core.Box;
import io.intino.datahub.box.DataHubBox;
import io.intino.magritte.framework.Graph;

public class Main {
	public static void main(String[] args) {
		Box box = new DataHubBox(args).put(new Graph().loadStashes("solution")).start();
		Runtime.getRuntime().addShutdownHook(new Thread(box::stop));
	}
}