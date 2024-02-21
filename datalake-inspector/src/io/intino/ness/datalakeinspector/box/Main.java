package io.intino.ness.datalakeinspector.box;

public class Main {
	public static void main(String[] args) {
		DatalakeInspectorBox box = new DatalakeInspectorBox(args);
		box.start();
		Runtime.getRuntime().addShutdownHook(new Thread(box::stop));
	}
}