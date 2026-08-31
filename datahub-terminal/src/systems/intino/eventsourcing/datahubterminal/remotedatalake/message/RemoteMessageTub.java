package systems.intino.eventsourcing.datahubterminal.remotedatalake.message;

import com.google.gson.JsonObject;
import io.intino.alexandria.Timetag;
import io.intino.alexandria.logger.Logger;
import jakarta.jms.BytesMessage;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import org.apache.activemq.BlobMessage;
import systems.intino.eventsourcing.datahubterminal.remotedatalake.DatalakeAccessor;
import systems.intino.eventsourcing.datalake.Datalake;
import systems.intino.eventsourcing.event.EventStream;
import systems.intino.eventsourcing.event.message.MessageEvent;
import systems.intino.eventsourcing.event.message.MessageEventReader;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import static systems.intino.eventsourcing.datahubterminal.remotedatalake.DatalakeAccessor.reflowSchema;

public class RemoteMessageTub implements Datalake.Store.Tub<MessageEvent> {
	private final DatalakeAccessor accessor;
	private final String tank;
	private final String source;
	private final String tub;

	public RemoteMessageTub(DatalakeAccessor accessor, String tank, String source, String tub) {
		this.accessor = accessor;
		this.tank = tank;
		this.source = source;
		this.tub = tub;
	}

	@Override
	public Timetag timetag() {
		return Timetag.of(tub);
	}

	@Override
	public EventStream<MessageEvent> events() {
		JsonObject jsonObject = reflowSchema(tank, source, List.of(tub));
		Message response = accessor.query(jsonObject.toString());
		if (response instanceof BlobMessage) return openStream((BlobMessage) response);
		if (response instanceof BytesMessage) return openStream((BytesMessage) response);
		return null;
	}

	private static EventStream<MessageEvent> openStream(BlobMessage message) {
		try {
			return new EventStream<>(new MessageEventReader(message.getInputStream()));
		} catch (IOException | JMSException e) {
			Logger.error(e);
			return null;
		}
	}

	private static EventStream<MessageEvent> openStream(BytesMessage message) {
		try {
			message.reset();
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			byte[] buffer = new byte[8192];
			for (int read = message.readBytes(buffer); read != -1; read = message.readBytes(buffer)) {
				output.write(buffer, 0, read);
			}
			return new EventStream<>(new MessageEventReader(new ByteArrayInputStream(output.toByteArray())));
		} catch (IOException | JMSException e) {
			Logger.error(e);
			return null;
		}
	}
}
