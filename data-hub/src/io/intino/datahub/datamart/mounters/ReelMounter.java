package io.intino.datahub.datamart.mounters;

import io.intino.alexandria.event.message.MessageEvent;
import io.intino.alexandria.logger.Logger;
import io.intino.alexandria.message.Message;
import io.intino.datahub.datamart.MasterDatamart;
import io.intino.datahub.model.Attribute;
import io.intino.datahub.model.Component;
import io.intino.datahub.model.Datamart;
import io.intino.datahub.model.Reel;
import io.intino.sumus.chronos.Reel.Shot;
import io.intino.sumus.chronos.Reel.State;
import io.intino.sumus.chronos.ReelFile;

import java.io.File;
import java.io.IOException;
import java.util.stream.Stream;

import static io.intino.datahub.box.DataHubBox.REEL_EXTENSION;

public final class ReelMounter extends MasterDatamartMounter {

	public ReelMounter(MasterDatamart datamart) {
		super(datamart);
	}

	@Override
	public void mount(Message message) {
		if (message == null) return;
		MessageEvent event = new MessageEvent(message);
		String ss = withoutParameters(event.ss());
		ReelFile reelFile = datamart.reelStore().get(ss);
		try {
			if (reelFile == null) reelFile = reelFile(ss);
			update(reelFile, event);
		} catch (IOException e) {
			Logger.error(e);
		}
	}

	private void update(ReelFile reelFile, MessageEvent event) throws IOException {
		Datamart datamart = this.datamart.definition();
		Reel reel = datamart.reel(r -> r.stateEvent().name$().equals(event.type()));
		if (reel == null) return;
		Reel.Mapping mapping = reel.mapping();
		String[] values = mappingAttribute(event.toMessage(), mapping.from());
		if (mapping.type().equals(Reel.Mapping.Type.Set))
			reelFile.set(event.ts(), values);
		else if (values.length == 1) {
			reelFile.append(new Shot(event.ts(), mapping.from().name$(), values[0].equals(State.On.name()) ? State.On : State.Off));
		}
	}

	private String[] mappingAttribute(Message message, Attribute from) {
		if (from.core$().owner().is(Component.class)) {
			return message.components().stream().flatMap(m -> values(message, from)).toArray(String[]::new);
		} else {
			return values(message, from).toArray(String[]::new);
		}
	}

	private static Stream<String> values(Message message, Attribute from) {
		Message.Value value = message.get(from.name$());
		if (!value.isNull()) {
			return value.asList(String.class).stream();
		}
		return Stream.empty();
	}


	private ReelFile reelFile(String ss) throws IOException {
		return ReelFile.open(new File(box().datamartTimelinesDirectory(datamart.name()), ss + REEL_EXTENSION));
	}

	private String withoutParameters(String ss) {
		return ss.contains("?") ? ss.substring(0, ss.indexOf("?")) : ss;
	}
}