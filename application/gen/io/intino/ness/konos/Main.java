package io.intino.ness.konos;

public class Main {
	public static void main(String[] args) {
		NessBox box = new NessBox(args).open();
		Runtime.getRuntime().addShutdownHook(new Thread(() -> box.close()));
	}
}