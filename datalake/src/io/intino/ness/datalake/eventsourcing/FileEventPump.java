package io.intino.ness.datalake.eventsourcing;

import io.intino.alexandria.inl.Message;
import io.intino.alexandria.zim.ZimStream;
import io.intino.ness.datalake.Datalake;

import java.util.Arrays;

public class FileEventPump implements EventPump {
	private final Datalake.EventStore store;

	public FileEventPump(Datalake.EventStore store) {
		this.store = store;
	}

	@Override
	public Reflow reflow(Reflow.Filter filter) {
		return new Reflow() {
			private ZimStream is = new ZimStream.Merge(tankInputStreams());

			ZimStream tankInputStream(Datalake.EventStore.Tank tank) {
				return tank.content(ts -> filter.allow(tank, ts));
			}

			private ZimStream[] tankInputStreams() {
				return store.tanks()
						.filter(filter::allow)
						.map(this::tankInputStream)
						.toArray(ZimStream[]::new);
			}

			@Override
			public void next(int blockSize, EventHandler... eventHandlers) {
				new ReflowBlock(is, eventHandlers).reflow(blockSize);
			}

		};
	}


	private static class ReflowBlock {
		private final ZimStream is;
		private final EventHandler[] eventHandlers;

		ReflowBlock(ZimStream is, EventHandler[] eventHandlers) {
			this.is = is;
			this.eventHandlers = eventHandlers;
		}

		void reflow(int blockSize) {
			terminate(process(blockSize));
		}

		private int process(int messages) {
			int pendingMessages = messages;
			while (is.hasNext() && pendingMessages-- >= 0) {
				Message message = is.next();
				Arrays.stream(eventHandlers).forEach(mh -> mh.handle(message));
			}
			return messages - pendingMessages;
		}

		private void terminate(int reflowedMessages) {
			Arrays.stream(eventHandlers)
					.filter(m -> m instanceof ReflowHandler)
					.map(m -> (ReflowHandler) m)
					.forEach(m -> terminate(m, reflowedMessages));
		}

		private void terminate(ReflowHandler reflowHandler, int reflowedMessages) {
			if (is.hasNext()) reflowHandler.onBlock(reflowedMessages);
			else reflowHandler.onFinish(reflowedMessages);
		}

	}
}
