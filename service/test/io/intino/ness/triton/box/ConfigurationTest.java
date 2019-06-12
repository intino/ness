package io.intino.ness.triton.box;

import io.intino.alexandria.zim.ZimStream;
import io.intino.ness.datalake.Datalake;
import io.intino.ness.ingestion.EventSession;
import io.intino.ness.ingestion.SessionHandler;
import io.intino.ness.triton.datalake.adapter.Context;

import java.io.InputStream;

public class ConfigurationTest {
	public static void adapt(io.intino.ness.triton.graph.Adapter self, ZimStream stream, Context context) {
		SessionHandler sessionHandler = new SessionHandler();
		EventSession eventSession = sessionHandler.createEventSession();
		while (stream.hasNext()) {
		}
	}

	public static void adaptDatalake(io.intino.ness.triton.graph.Adapter self, Datalake datalake, Context context) {
	}

	public static void configureAdapter(io.intino.ness.triton.graph.Adapter self, Context context, String configuration, InputStream attachment) {
		;
	}
}