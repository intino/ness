package io.intino.datahub.datalake.pump;


import io.intino.alexandria.datalake.Datalake;
import io.intino.alexandria.event.Event;
import io.intino.alexandria.event.EventStream;

import java.util.Arrays;
import java.util.Iterator;
import java.util.stream.Stream;

public class FileEventPump implements EventPump {
	private final Datalake.Store<? extends Event> store;

	public FileEventPump(Datalake.Store<? extends Event> store) {
		this.store = store;
	}

	@Override
	public Reflow reflow(Reflow.Filter filter) {
		return new Reflow() {
			private final Stream<Stream<Event>> streamStream = tankInputStreams();
			private final Iterator<? extends Event> iterator = EventStream.merge(streamStream).iterator();

			private Stream<Stream<Event>> tankInputStreams() {
				return store.tanks()
						.filter(filter::allow)
						.map(this::tankInputStream);
			}

			Stream<Event> tankInputStream(Datalake.Store.Tank<? extends Event> tank) {
				return (Stream<Event>) tank.content((ss, tt) -> filter.allow(tank, ss.toString(), tt));
			}

			public boolean hasNext() {
				return iterator.hasNext();
			}

			@Override
			public void next(int blockSize, EventHandler... eventHandlers) {
				new ReflowBlock(iterator, eventHandlers).reflow(blockSize);
			}

		};

	}


	private static class ReflowBlock {
		private final Iterator<? extends Event> is;
		private final EventHandler[] eventHandlers;

		ReflowBlock(Iterator<? extends Event> is, EventHandler[] eventHandlers) {
			this.is = is;
			this.eventHandlers = eventHandlers;
		}

		void reflow(int blockSize) {
			terminate(process(blockSize));
		}

		private int process(int messages) {
			int pendingMessages = messages;
			while (is.hasNext() && pendingMessages-- >= 0) {
				Event event = is.next();
				Arrays.stream(eventHandlers).forEach(mh -> mh.handle(event));
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

