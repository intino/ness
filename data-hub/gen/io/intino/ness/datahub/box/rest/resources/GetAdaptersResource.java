package io.intino.ness.datahub.box.rest.resources;

import java.util.List;
import java.util.ArrayList;
import io.intino.alexandria.exceptions.*;
import io.intino.ness.datahub.box.*;
import io.intino.alexandria.core.Box;
import io.intino.alexandria.rest.Resource;
import io.intino.alexandria.rest.spark.SparkManager;
import io.intino.alexandria.rest.spark.SparkPushService;



public class GetAdaptersResource implements Resource {

	private DataHubBox box;
	private SparkManager<SparkPushService> manager;

	public GetAdaptersResource(DataHubBox box, SparkManager manager) {
		this.box = box;
		this.manager = manager;

	}

	public void execute() throws Unknown {
		write(fill(new io.intino.ness.datahub.box.actions.GetAdaptersAction()).execute());
	}

	private io.intino.ness.datahub.box.actions.GetAdaptersAction fill(io.intino.ness.datahub.box.actions.GetAdaptersAction action) {
		action.box = this.box;
		action.context = context();
		return action;
	}

	private void write(List<String> object) {
		manager.write(object);
	}

	private io.intino.alexandria.core.Context context() {
		io.intino.alexandria.core.Context context = new io.intino.alexandria.core.Context();
		context.put("domain", manager.domain());
		context.put("baseUrl", manager.baseUrl());
		context.put("requestUrl", manager.baseUrl() + manager.request().pathInfo());

		return context;
	}
}