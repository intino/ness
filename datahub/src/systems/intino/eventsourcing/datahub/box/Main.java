package systems.intino.eventsourcing.datahub.box;


public class Main {
	public static void main(String[] args) {
		DatahubBox box = new DatahubBox(args);
		box.start();
		Runtime.getRuntime().addShutdownHook(new Thread(box::stop));
	}
}