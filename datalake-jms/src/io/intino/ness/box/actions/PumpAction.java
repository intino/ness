package io.intino.ness.box.actions;

import io.intino.konos.alexandria.functions.MessageMapper;
import io.intino.ness.box.NessBox;
import io.intino.ness.datalake.graph.Tank;
import io.intino.ness.graph.Pipe;
import io.intino.ness.inl.Message;

import java.time.Instant;
import java.util.Iterator;

public class PumpAction extends Action {

	public NessBox box;
	public String input;
	public String output;

	public String execute() {
		final Tank origin = box.datalake().tank(input);
		final Tank destination = box.datalake().tank(output);
		if (origin == null || destination == null) return "Tank not found";
		final Pipe pipe = box.nessGraph().pipeList().stream().filter(p -> p.origin().equals(origin.feedQN()) && p.destination().equals(origin.feedQN())).findFirst().orElse(null);
		if (pipe == null) return "No pipe found";
		pump(pipe, origin, destination);
		return OK;
	}

	private void pump(Pipe pipe, Tank from, Tank to) {
		final Iterator<Message> iterator = from.sortedMessagesIterator(Instant.MIN);
		while (!iterator.hasNext()) {
			Message next = iterator.next();
			next = pipe.transformer() != null && pipe.transformer() instanceof MessageMapper ? ((MessageMapper) pipe.transformer()).map(next) : next;
			if (next != null) to.drop(next);
		}
	}
}