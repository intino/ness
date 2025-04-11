package systems.intino.eventsourcing.datahub.box.service.jms;

import systems.intino.eventsourcing.datahub.box.DatahubBox;
import systems.intino.eventsourcing.datahub.box.actions.SealAction;
import systems.intino.eventsourcing.event.EventHub.RequestConsumer;
import systems.intino.eventsourcing.message.Message;
import systems.intino.eventsourcing.message.MessageReader;

public class SealRequest implements RequestConsumer {
	private final DatahubBox box;

	public SealRequest(DatahubBox box) {
		this.box = box;
	}

	public String accept(String request) {
		try {
			Message next = new MessageReader(request).next();
			String stage = next.get("stage").data();
			String result;
			if (stage != null) result = new SealAction(box).execute(stage);
			else result = new SealAction(box).execute();
			return result.replace(SealAction.MESSAGE_PREFIX, "");
		} catch (Throwable e) {
			io.intino.alexandria.logger.Logger.error(e.getMessage(), e);
			return null;
		}
	}
}