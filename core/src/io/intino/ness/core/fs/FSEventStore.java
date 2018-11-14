package io.intino.ness.core.fs;

import io.intino.alexandria.inl.Message;
import io.intino.alexandria.zim.ZimStream;
import io.intino.alexandria.zim.ZimStream.Merge;
import io.intino.ness.core.Datalake;

import java.io.File;
import java.util.Arrays;
import java.util.stream.Stream;

import static io.intino.alexandria.zim.ZimReader.ZimExtension;

public class FSEventStore implements Datalake.EventStore {
	public static final String EventExtension = ZimExtension;
	public static final String SessionExtension = ".event.blob";
	private File root;

	public FSEventStore(File root) {
		this.root = root;
	}

	@Override
	public Stream<Tank> tanks() {
		return FS.foldersIn(root).map(FSEventTank::new);
	}

	@Override
	public Tank tank(String name) {
		return new FSEventTank(new File(root, name));
	}

	@Override
	public Reflow reflow(Reflow.Filter filter) {
		return new Reflow() {
			private ZimStream is = new Merge(tankInputStreams());

			ZimStream tankInputStream(Tank tank) {
				return tank.content(ts -> filter.allow(tank, ts));
			}

			private ZimStream[] tankInputStreams() {
				return tanks()
						.filter(filter::allow)
						.map(this::tankInputStream)
						.toArray(ZimStream[]::new);
			}

			@Override
			public void next(int blockSize, MessageHandler... messageHandlers) {
				new ReflowBlock(is, messageHandlers).reflow(blockSize);
			}

		};
	}

	@Override
	public Subscription subscribe(Tank tank) {
		return (clientId, messageHandlers) -> {

		};
	}

	@Override
	public void unsubscribe(Tank tank) {
	}

	private static class ReflowBlock {
		private final ZimStream is;
		private final MessageHandler[] messageHandlers;

		ReflowBlock(ZimStream is, MessageHandler[] messageHandlers) {
			this.is = is;
			this.messageHandlers = messageHandlers;
		}

		void reflow(int blockSize) {
			terminate(process(blockSize));
		}

		private int process(int messages) {
			int pendingMessages = messages;
			while (is.hasNext() && pendingMessages-- >= 0) {
				Message message = is.next();
				Arrays.stream(messageHandlers).forEach(mh -> mh.handle(message));
			}
			return messages - pendingMessages;
		}

		private void terminate(int reflowedMessages) {
			Arrays.stream(messageHandlers)
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
