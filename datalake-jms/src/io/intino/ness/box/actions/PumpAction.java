package io.intino.ness.box.actions;

import io.intino.konos.alexandria.functions.MessageMapper;
import io.intino.ness.box.NessBox;
import io.intino.ness.datalake.graph.Tank;
import io.intino.ness.graph.Function;
import io.intino.ness.inl.Message;

import java.time.Instant;
import java.util.Iterator;

public class PumpAction extends Action {

	public NessBox box;
	public String functionName;
	public String input;
	public String output;

	public String execute() {
		Function function = box.nessGraph().functionList(f -> f.name$().equals(functionName)).findFirst().orElse(null);
		if (function == null) return "Function not found";
		if (box.datalake().tank(input) == null || box.datalake().tank(output) == null) return "Function not found";
		pump(box.datalake().tank(input), box.datalake().tank(output), function);
		return OK;
	}

	private void pump(Tank from, Tank to, Function function) {
		final Iterator<Message> iterator = from.sortedMessagesIterator(Instant.MIN);
		while (!iterator.hasNext()) {
			Message next = iterator.next();
			next = function.aClass() instanceof MessageMapper ? ((MessageMapper) function.aClass()).map(next) : null;
			if (next != null) to.drop(next);
		}
	}
}