package io.intino.datahub.box;


public class Main {
	public static void main(String[] args) {
		DataHubBox box = new DataHubBox(args);
		box.start();
		Runtime.getRuntime().addShutdownHook(new Thread(box::stop));
	}
}