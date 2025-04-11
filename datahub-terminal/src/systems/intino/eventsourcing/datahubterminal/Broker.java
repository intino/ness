package systems.intino.eventsourcing.datahubterminal;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class Broker {
	public static boolean isRunning(String brokerUrl) {
		String[] values = brokerUrl.substring(brokerUrl.indexOf("//") + 2).replace(")", "").split(":");
		if (values.length == 1) return false;
		final int port = Integer.parseInt(values[1].contains("?") ? values[1].split("\\?")[0] : values[1]);
		return isRunning(values[0], port);
	}

	private static boolean isRunning(String address, int port) {
		try {
			try (Socket socket = new Socket()) {
				socket.connect(new InetSocketAddress(address, port), 5000);
			}
			return true;
		} catch (IOException e) {
			return false;
		}
	}
}
