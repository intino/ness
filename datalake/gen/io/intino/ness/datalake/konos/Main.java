package io.intino.ness.datalake.konos;



public class Main {

	public static void main(String[] args) {
		DatalakeConfiguration configuration = createConfigurationFromArgs(args);

		DatalakeBox box = run(configuration);
		Runtime.getRuntime().addShutdownHook(new Thread(() -> box.quit()));
	}

	private static DatalakeConfiguration createConfigurationFromArgs(String[] args) {
		return new DatalakeConfiguration(args);
	}

	private static DatalakeBox run(DatalakeConfiguration configuration) {
		DatalakeBox box = new DatalakeBox(configuration);
		Setup.configureBox(box);
		box.init();
		return box;
	}
}