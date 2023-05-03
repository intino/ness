package io.intino.datahub.box.service.jms;

import io.intino.alexandria.Scale;
import io.intino.alexandria.Timetag;
import io.intino.alexandria.jms.MessageReader;
import io.intino.alexandria.logger.Logger;
import io.intino.datahub.box.DataHubBox;
import io.intino.datahub.datamart.MasterDatamart;
import org.apache.activemq.command.ActiveMQBytesMessage;
import org.apache.activemq.command.ActiveMQTextMessage;

import javax.jms.Message;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.intino.datahub.box.DataHubBox.REEL_EXTENSION;
import static io.intino.datahub.box.DataHubBox.TIMELINE_EXTENSION;

/**
 * <p>Handles datamart requests. The request is a string with key-value pairs, separated by ;</p>
 * <p>For example:</p>
 * <p><i>datamart=my_datamart;operation=entities;timetag=20230101</i></p>
 *
 * <ul>
 * <li>The entry 'datamart' must be present and must be the first in the request</li>
 * <li>The entry 'operation' must be present</li>
 * </ul>
 *
 * */
public class DatamartsRequest {

	private final DataHubBox box;

	public DatamartsRequest(DataHubBox box) {
		this.box = box;
	}

	public Stream<Message> accept(Message request) {
		try {
			String content = MessageReader.textFrom(request);
			return handleDatamartDownload(content);
		} catch (Throwable e) {
			Logger.error(e);
			return Stream.empty();
		}
	}

	private Stream<Message> handleDatamartDownload(String request) {
		Map<String, String> args = parseArgumentsFrom(request);

		String datamartName = args.get("datamart");
		MasterDatamart datamart = box.datamarts().get(datamartName);
		if(datamart == null) {
			Logger.error("Datamart " + datamartName + " not found");
			return Stream.empty();
		}

		return switch(args.get("operation")) {
			case "snapshots" -> listAvailableSnapshotsOf(datamart);
			case "entities" -> downloadEntities(datamart, args);
			case "list-timelines" -> listTimelineFiles(datamart, args);
			case "get-timeline" -> downloadTimeline(datamart, args);
			case "list-reels" -> listReelFiles(datamart, args);
			case "get-reel" -> downloadReel(datamart, args);
			default -> Stream.empty();
		};
	}

	private Stream<Message> downloadReel(MasterDatamart datamart, Map<String, String> args) {
		return downloadChronos(args, box.datamartReelsDirectory(datamart.name()), REEL_EXTENSION);
	}

	private Stream<Message> downloadTimeline(MasterDatamart datamart, Map<String, String> args) {
		return downloadChronos(args, box.datamartTimelinesDirectory(datamart.name()), TIMELINE_EXTENSION);
	}

	private Stream<Message> downloadChronos(Map<String, String> args, File dir, String extension) {
		String id = args.get("id");
		if(id == null) {
			Logger.error("Chronos object download requested but id argument not found");
			return Stream.empty();
		}
		File file = new File(dir, id + extension);
		if(!file.exists()) return Stream.empty();

		String mode = args.getOrDefault("mode", "download");
		return mode.equals("path") ? path(file) : download(file);
	}

	private Stream<Message> download(File file) {
		try {
			ActiveMQBytesMessage message = new ActiveMQBytesMessage();
			byte[] bytes = Files.readAllBytes(file.toPath());
			message.setProperty("name", file.getName());
			message.setIntProperty("size", bytes.length);
			message.writeBytes(bytes);
			message.compress();
			return Stream.of(message);
		} catch (Exception e) {
			Logger.error("Could not send file " + file.getAbsolutePath() + ": " + e.getMessage(), e);
			return Stream.empty();
		}
	}

	private Stream<Message> path(File file) {
		try {
			ActiveMQTextMessage message = new ActiveMQTextMessage();
			message.setText(file.getAbsolutePath());
			return Stream.of(message);
		} catch (Exception e) {
			Logger.error("Could not send file path " + file.getAbsolutePath() + ": " + e.getMessage(), e);
			return Stream.empty();
		}
	}

	private Stream<Message> listReelFiles(MasterDatamart datamart, Map<String, String> args) {
		return listFiles(datamart.name(), box.datamartReelFiles(datamart.name()));
	}

	private Stream<Message> listTimelineFiles(MasterDatamart datamart, Map<String, String> args) {
		return listFiles(datamart.name(), box.datamartTimelineFiles(datamart.name()));
	}

	private Stream<Message> listFiles(String datamart, List<File> files) {
		try {
			ActiveMQTextMessage message = new ActiveMQTextMessage();
			message.setText(files.stream().map(File::getAbsolutePath).collect(Collectors.joining(",")));
			message.setIntProperty("count", files.size());
			return Stream.of(message);
		} catch (Exception e) {
			Logger.error("Could not list chronos files of " + datamart + ": " + e.getMessage(), e);
			return Stream.empty();
		}
	}

	private Stream<Message> downloadEntities(MasterDatamart datamart, Map<String, String> args) {
		Optional<String> timetag = Optional.ofNullable(args.get("timetag"));
		return timetag.map(s -> box.datamartSerializer().loadMostRecentSnapshotTo(datamart.name(), asTimetag(s))
				.map(MasterDatamart.Snapshot::datamart)
				.map(this::downloadEntities)
				.orElse(Stream.empty())).orElseGet(() -> datamart == null ? Stream.empty() : downloadEntities(datamart));
	}

	private Timetag asTimetag(String timetag) {
		return timetag.isEmpty() ? Timetag.of(LocalDate.now(), Scale.Day) : Timetag.of(timetag);
	}

	private Stream<Message> listAvailableSnapshotsOf(MasterDatamart datamart) {
		List<Timetag> snapshots = box.datamartSerializer().listAvailableSnapshotsOf(datamart.name());
		if(snapshots.isEmpty()) return Stream.empty();
		try {
			ActiveMQTextMessage message = new ActiveMQTextMessage();
			message.setIntProperty("count", snapshots.size());
			message.setText(snapshots.stream().map(Timetag::value).collect(Collectors.joining(",")));
			return Stream.of(message);
		} catch (Exception e) {
			Logger.error(e);
			return Stream.empty();
		}
	}

	private Stream<Message> downloadEntities(MasterDatamart datamart) {
		try {
			ActiveMQBytesMessage message = new ActiveMQBytesMessage();
			message.setIntProperty("count", datamart.entityStore().size());
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream(16 * 1024);
			box.datamartSerializer().serialize(datamart, outputStream);
			byte[] bytes = outputStream.toByteArray();
			message.writeBytes(bytes);
			message.setIntProperty("size", bytes.length);
			return Stream.of(message);
		} catch (Exception e) {
			Logger.error(e);
			return Stream.empty();
		}
	}

	private Map<String, String> parseArgumentsFrom(String request) {
		String[] command = request.split(";", -1);
		Map<String, String> args = new LinkedHashMap<>(command.length - 1);
		for(String argument : command) {
			String[] entry = argument.split("=", 2);
			if (entry.length < 2) continue;
			args.put(entry[0].trim(), entry[1].trim());
		}
		return args;
	}
}