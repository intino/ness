package io.intino.ness.triton.box;

import io.intino.alexandria.zim.ZimStream;
import io.intino.ness.ingestion.EventSession;
import io.intino.ness.ingestion.SessionHandler;
import io.intino.ness.triton.datalake.adapter.Context;

public class ConfigurationTest {
	public static void adapt(io.intino.ness.triton.graph.Adapter self, ZimStream stream, Context context) {
		SessionHandler sessionHandler = new SessionHandler();
		EventSession eventSession = sessionHandler.createEventSession();
		while (stream.hasNext()) {
		}
	}
}
