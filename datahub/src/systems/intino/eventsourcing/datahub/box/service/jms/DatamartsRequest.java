package systems.intino.eventsourcing.datahub.box.service.jms;

import io.intino.alexandria.Timetag;
import io.intino.alexandria.logger.Logger;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import org.apache.activemq.command.ActiveMQBytesMessage;
import org.apache.activemq.command.ActiveMQTextMessage;
import systems.intino.eventsourcing.datahub.box.DatahubBox;
import systems.intino.eventsourcing.datahub.datamart.MasterDatamart;
import systems.intino.eventsourcing.datahub.datamart.snapshots.DatamartSnapshotManager;
import systems.intino.eventsourcing.jms.MessageReader;

import java.io.File;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <p>Handles datamart requests. The request is a string with key-value pairs, separated by ;</p>
 * <p>For example:</p>
 * <p><i>datamart=my_datamart;operation=entities;timetag=20230101</i></p>
 *
 * <ul>
 * <li>The entry 'datamart' must be present and must be the first in the request</li>
 * <li>The entry 'operation' must be present</li>
 * </ul>
 */
public class DatamartsRequest {

	private final DatahubBox box;

	public DatamartsRequest(DatahubBox box) {
		this.box = box;
	}

	public Stream<Message> accept(Message request) {
		try {
			String content = MessageReader.textFrom(request);
			return handleDatamartDownload(content);
		} catch (Throwable e) {
			Logger.error(e);
			return errorMessage(e.getMessage());
		}
	}

	private Stream<Message> handleDatamartDownload(String request) {
		Map<String, String> args = parseArgumentsFrom(request);
		String datamartName = args.get("datamart");
		MasterDatamart datamart = box.datamarts().get(datamartName);
		if (datamart == null) {
			String message = "Datamart " + datamartName + " not found";
			Logger.error(message);
			return errorMessage(message);
		}

		return switch (args.get("operation")) {
			case "snapshots" -> listAvailableSnapshotsOf(datamart);
			case "source" -> source(datamart, args);
			default -> errorMessage("Operation " + args.get("operation") + " not found");
		};
	}

	private Stream<Message> errorMessage(String errorDescription) {
		ActiveMQTextMessage message = new ActiveMQTextMessage();
		try {
			message.setBooleanProperty("success", false);
			message.setText(errorDescription);
			return Stream.of(message);
		} catch (JMSException e) {
			Logger.error(e);
			return Stream.of(message);
		}
	}

	private Stream<Message> successEmptyResponse() {
		ActiveMQTextMessage message = new ActiveMQTextMessage();
		try {
			message.setBooleanProperty("success", true);
			return Stream.of(message);
		} catch (JMSException e) {
			Logger.error(e);
			return Stream.of(message);
		}
	}

	private Stream<Message> download(File file) {
		try {
			ActiveMQBytesMessage message = new ActiveMQBytesMessage();
			byte[] bytes = Files.readAllBytes(file.toPath());
			message.setProperty("name", file.getName());
			message.setBooleanProperty("success", true);
			message.setIntProperty("size", bytes.length);
			message.writeBytes(bytes);
			message.compress();
			return Stream.of(message);
		} catch (Exception e) {
			return errorMessage("Could not send file " + file.getAbsolutePath() + ": " + e.getMessage());
		}
	}

	private Stream<Message> path(File file) {
		try {
			ActiveMQTextMessage message = new ActiveMQTextMessage();
			message.setBooleanProperty("success", true);
			message.setText(file.getAbsolutePath());
			return Stream.of(message);
		} catch (Exception e) {
			String message = "Could not send file path " + file.getAbsolutePath() + ": " + e.getMessage();
			Logger.error(message, e);
			return errorMessage(message);
		}
	}

	private Stream<Message> source(String datamart, String source) {
		try {
			ActiveMQTextMessage message = new ActiveMQTextMessage();
			message.setBooleanProperty("success", true);
			message.setText(source);
			return Stream.of(message);
		} catch (Exception e) {
			String message = "Could not list chronos files of " + datamart + ": " + e.getMessage();
			Logger.error(message, e);
			return errorMessage(message);
		}
	}

	private Stream<Message> source(MasterDatamart datamart, Map<String, String> args) {
		return source(datamart.name(), box.datamartSource(datamart.name()));
	}

	private Stream<Message> listAvailableSnapshotsOf(MasterDatamart datamart) {
		List<Timetag> snapshots = new DatamartSnapshotManager(box).listAvailableSnapshotsOf(datamart.name());
		if (snapshots.isEmpty()) return successEmptyResponse();
		try {
			ActiveMQTextMessage message = new ActiveMQTextMessage();
			message.setBooleanProperty("success", true);
			message.setIntProperty("count", snapshots.size());
			message.setText(snapshots.stream().map(Timetag::value).collect(Collectors.joining(",")));
			return Stream.of(message);
		} catch (Exception e) {
			Logger.error(e);
			return errorMessage(e.getMessage());
		}
	}


	private Map<String, String> parseArgumentsFrom(String request) {
		String[] command = request.split(";", -1);
		Map<String, String> args = new LinkedHashMap<>(command.length - 1);
		for (String argument : command) {
			String[] entry = argument.split("=", 2);
			if (entry.length < 2) continue;
			args.put(entry[0].trim(), entry[1].trim());
		}
		return args;
	}
}