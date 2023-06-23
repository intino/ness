package io.intino.datahub.datamart.mounters;

import io.intino.alexandria.event.Event;
import io.intino.alexandria.event.message.MessageEvent;
import io.intino.alexandria.logger.Logger;
import io.intino.alexandria.message.Message;
import io.intino.datahub.datamart.MasterDatamart;
import io.intino.datahub.model.Attribute;
import io.intino.datahub.model.Datamart;
import io.intino.datahub.model.Reel;
import io.intino.sumus.chronos.ReelFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;

import static io.intino.datahub.box.DataHubBox.REEL_EXTENSION;
import static java.io.File.separator;

public final class ReelMounter extends MasterDatamartMounter {

	public ReelMounter(MasterDatamart datamart) {
		super(datamart);
	}

	@Override
	public void mount(Event event) {
		synchronized (datamart) {
			if (event instanceof MessageEvent e) mount(e.toMessage());
		}
	}

	@Override
	public void mount(Message message) {
		if (message == null) return;
		MessageEvent event = new MessageEvent(message);
		String ss = withoutParameters(event.ss());
		ReelFile reelFile = datamart.reelStore().get(ss, ss);
		try {
			if (reelFile == null) reelFile = reelFile(message.type(), subject(event));
			update(reelFile, event);
		} catch (IOException e) {
			Logger.error(e);
		}
	}

	private String subject(MessageEvent event) {
		Datamart datamart = this.datamart.definition();
		Reel reel = datamart.reel(r -> r.tank().message().name$().equals(event.type()));
		return event.toMessage().get(reel.entitySource().name$()).asString();
	}

	private void update(ReelFile reelFile, MessageEvent event) throws IOException {
		Datamart datamart = this.datamart.definition();
		List<Reel> reels = datamart.reelList(r -> r.tank().name$().equals(event.type()));
		for (Reel reel : reels)
			reelFile.set(event.ts(), group(event, reel.groupSource()), mappingAttribute(event.toMessage(), reel));
	}

	private String group(MessageEvent event, Attribute attribute) {
		Message.Value value = event.toMessage().get(attribute.name$());
		return !value.isNull() ? value.asString() : null;
	}

	private String[] mappingAttribute(Message message, Reel reel) {
		return values(message, reel.signals()).toArray(String[]::new);
	}

	private static Stream<String> values(Message message, Attribute from) {
		Message.Value value = message.get(from.name$());
		return !value.isNull() ? value.asList(String.class).stream() : Stream.empty();
	}

	private ReelFile reelFile(String type, String subject) throws IOException {
		File file = new File(box().datamartReelsDirectory(datamart.name()), type + separator + subject + REEL_EXTENSION);
		file.getParentFile().mkdirs();
		return file.exists() ? ReelFile.open(file) : ReelFile.create(file);
	}

	private String withoutParameters(String ss) {
		return ss.contains("?") ? ss.substring(0, ss.indexOf("?")) : ss;
	}
}