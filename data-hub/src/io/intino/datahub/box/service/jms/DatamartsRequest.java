package io.intino.datahub.box.service.jms;

import io.intino.alexandria.Scale;
import io.intino.alexandria.Timetag;
import io.intino.alexandria.jms.MessageReader;
import io.intino.alexandria.logger.Logger;
import io.intino.datahub.box.DataHubBox;
import io.intino.datahub.datamart.MasterDatamart;
import io.intino.datahub.datamart.serialization.MasterDatamartSerializer;
import io.intino.datahub.datamart.serialization.MasterDatamartSnapshots;
import org.apache.activemq.command.ActiveMQBytesMessage;
import org.apache.activemq.command.ActiveMQTextMessage;

import javax.jms.Message;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.intino.datahub.datamart.serialization.MasterDatamartSnapshots.loadMostRecentSnapshotTo;

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
		String[] command = request.split(":", 3);
		if(command.length < 3) return fail("Datamart requests must be like this: datamart:<name>:[snapshots | timetag], but it was " + request);

		String datamartName = command[1].trim();
		String operation = command[2].trim();

		return operation.equals("snapshots")
				? listAvailableSnapshotsOf(datamartName)
				: downloadDatamart(datamartName, operation);
	}

	private Stream<Message> downloadDatamart(String datamartName, String timetag) {
		if(timetag.isEmpty()) {
			MasterDatamart<?> datamart = box.datamarts().get(datamartName);
			return datamart == null ? Stream.empty() : downloadDatamart(datamart);
		}
		return loadMostRecentSnapshotTo(box.datamarts().root(), datamartName, asTimetag(timetag), box.graph())
				.map(MasterDatamart.Snapshot::datamart)
				.map(this::downloadDatamart)
				.orElse(Stream.empty());
	}

	private Timetag asTimetag(String timetag) {
		return timetag.isEmpty() ? Timetag.of(LocalDate.now(), Scale.Day) : Timetag.of(timetag);
	}

	private Stream<Message> listAvailableSnapshotsOf(String datamart) {
		List<Timetag> snapshots = MasterDatamartSnapshots.listAvailableSnapshotsOf(box.datamarts().root(), datamart);
		if(snapshots.isEmpty()) return Stream.empty();
		try {
			ActiveMQTextMessage message = new ActiveMQTextMessage();
			message.setIntProperty("size", snapshots.size());
			message.setText(snapshots.stream().map(Timetag::value).collect(Collectors.joining(",")));
			return Stream.of(message);
		} catch (Exception e) {
			Logger.error(e);
			return Stream.empty();
		}
	}

	private Stream<Message> downloadDatamart(MasterDatamart<?> datamart) {
		try {
			ActiveMQBytesMessage message = new ActiveMQBytesMessage();
			message.setIntProperty("size", datamart.size());
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream(8192);
			MasterDatamartSerializer.serialize(datamart, outputStream);
			byte[] bytes = outputStream.toByteArray();
			message.writeBytes(bytes);
			message.setIntProperty("content-size", bytes.length);
			return Stream.of(message);
		} catch (Exception e) {
			Logger.error(e);
			return Stream.empty();
		}
	}

	private static <T> Stream<T> fail(String msg) {
		Logger.error(msg);
		return Stream.empty();
	}
}