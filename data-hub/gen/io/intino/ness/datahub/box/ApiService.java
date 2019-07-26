package io.intino.ness.datahub.box;

import io.intino.alexandria.rest.AlexandriaSpark;
import io.intino.ness.datahub.box.rest.resources.*;
import io.intino.alexandria.core.Box;
import io.intino.alexandria.rest.security.DefaultSecurityManager;
import io.intino.alexandria.rest.spark.SparkPushService;

public class ApiService {

	public static AlexandriaSpark setup(AlexandriaSpark server, DataHubBox box) {
		server.route("api/adapter/:name/run").post(manager -> new PostRunResource(box, manager).execute());
		server.route("api/adapter/:name/configure").post(manager -> new PostConfigureResource(box, manager).execute());
		server.route("api/adapters").get(manager -> new GetAdaptersResource(box, manager).execute());

		return server;
	}
}