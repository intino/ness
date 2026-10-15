import org.example.test.ness.GraphLoader;
import org.junit.Assert;
import org.junit.Test;
import systems.intino.eventsourcing.datahub.box.DatahubBox;
import systems.intino.eventsourcing.datahubterminal.Broker;
import systems.intino.eventsourcing.datahubterminal.JmsConnector;
import systems.intino.eventsourcing.event.message.MessageEvent;
import systems.intino.eventsourcing.jms.ConnectionConfig;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

public class OutboxRecoveryIntegrationTest {

	private static final Duration TIMEOUT = Duration.ofSeconds(30);
	private static final String TANK = "inventario.Inventario";

	@Test
	public void recoversOutboxEventsWhenDatahubConnectionResumes() throws Exception {
		Path home = new File("temp").toPath();
		int brokerPort = freePort();
		String brokerUrl = "failover:(tcp://127.0.0.1:" + brokerPort + ")?initialReconnectDelay=1000&maxReconnectDelay=1000&maxReconnectAttempts=-1&useExponentialBackOff=false";
		Path outbox = home.resolve("terminal-outbox");
		Path brokerStage = home.resolve("datahub").resolve("broker").resolve("stage");
		DatahubBox datahub = null;
		DatahubBox restartedDatahub = null;
		boolean initialBrokerStopped = false;
		JmsConnector terminal = null;
		String firstMarker = "outbox-first-" + UUID.randomUUID();
		String secondMarker = "outbox-second-" + UUID.randomUUID();

		try {
			datahub = startDatahub(home, brokerPort);
			DatahubBox runningDatahub = datahub;
			terminal = new JmsConnector(
					new ConnectionConfig(brokerUrl, "inventario", "inventario", "outbox-recovery-" + UUID.randomUUID()),
					outbox.toFile());
			terminal.start();

			JmsConnector connectedTerminal = terminal;
			await("the terminal to connect to Datahub", () -> connectedTerminal.connection() != null && connectedTerminal.session() != null);

			runningDatahub.brokerService().stop();
			initialBrokerStopped = true;
			await("the Datahub broker to stop", () -> !Broker.isRunning(brokerUrl));

			terminal.sendEvent(TANK, new MarkedEvent(firstMarker));
			terminal.sendEvent(TANK, new MarkedEvent(secondMarker));

			Path eventsOutbox = outbox.resolve("events");
			await("both events to be persisted in the outbox", () -> outboxEvents(eventsOutbox) == 2);
			Assert.assertFalse("Events must not reach Datahub while its broker is stopped",
					stageContains(brokerStage, firstMarker, secondMarker));

			restartedDatahub = startDatahub(home, brokerPort);
			await("the recovered events to be serialized by Datahub",
					() -> stageContains(brokerStage, firstMarker, secondMarker));
			await("the event outbox to be emptied after recovery", () -> outboxEvents(eventsOutbox) == 0);
		} finally {
			if (terminal != null) terminal.stop();
			if (restartedDatahub != null && restartedDatahub.brokerService() != null)
				restartedDatahub.brokerService().stop();
			if (!initialBrokerStopped && datahub != null && datahub.brokerService() != null)
				datahub.brokerService().stop();
			deleteRecursively(home);
		}
	}

	private static DatahubBox startDatahub(Path home, int brokerPort) {
		DatahubBox datahub = new DatahubBox(new String[]{
				"home=" + home,
				"backup_directory=" + home.resolve("backup"),
				"broker_port=" + brokerPort,
				"broker_secondary_port=0"
		});
		datahub.put(GraphLoader.load().core$());
		datahub.start();
		return datahub;
	}

	private static long outboxEvents(Path eventsOutbox) throws IOException {
		if (!Files.isDirectory(eventsOutbox)) return 0;
		try (Stream<Path> files = Files.list(eventsOutbox)) {
			return files.filter(file -> file.getFileName().toString().endsWith(".inl")).count();
		}
	}

	private static boolean stageContains(Path stage, String... markers) throws IOException {
		if (!Files.isDirectory(stage)) return false;
		String content;
		try (Stream<Path> files = Files.walk(stage)) {
			content = files.filter(Files::isRegularFile)
					.map(OutboxRecoveryIntegrationTest::readFile)
					.reduce("", String::concat);
		}
		for (String marker : markers)
			if (!content.contains(marker)) return false;
		return true;
	}

	private static String readFile(Path file) {
		try {
			return Files.readString(file);
		} catch (IOException e) {
			return "";
		}
	}

	private static void await(String expectation, CheckedCondition condition) throws Exception {
		long deadline = System.nanoTime() + TIMEOUT.toNanos();
		Throwable lastFailure = null;
		while (System.nanoTime() < deadline) {
			try {
				if (condition.matches()) return;
				lastFailure = null;
			} catch (Throwable e) {
				lastFailure = e;
			}
			Thread.sleep(50);
		}
		String detail = lastFailure == null ? "" : ": " + lastFailure.getMessage();
		Assert.fail("Timed out waiting for " + expectation + detail);
	}

	private static int freePort() throws IOException {
		try (ServerSocket socket = new ServerSocket(0)) {
			return socket.getLocalPort();
		}
	}

	private static void deleteRecursively(Path directory) throws IOException {
		if (directory == null || !Files.exists(directory)) return;
		List<Path> paths;
		try (Stream<Path> files = Files.walk(directory)) {
			paths = files.sorted(Comparator.reverseOrder()).toList();
		}
		for (Path path : paths) Files.deleteIfExists(path);
	}

	@FunctionalInterface
	private interface CheckedCondition {
		boolean matches() throws Exception;
	}

	private static class MarkedEvent extends MessageEvent {
		private final Instant timestamp = Instant.now();

		private MarkedEvent(String marker) {
			super("Inventario", "inventario");
			super.toMessage().set("marker", marker);
		}

		@Override
		public Instant ts() {
			return timestamp;
		}

		@Override
		public Format format() {
			return Format.Message;
		}
	}
}
