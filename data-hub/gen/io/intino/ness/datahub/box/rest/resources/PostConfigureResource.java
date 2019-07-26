package io.intino.ness.datahub.box.rest.resources;

import java.util.List;
import java.util.ArrayList;
import io.intino.alexandria.exceptions.*;
import io.intino.ness.datahub.box.*;
import io.intino.alexandria.core.Box;
import io.intino.alexandria.rest.Resource;
import io.intino.alexandria.rest.spark.SparkManager;
import io.intino.alexandria.rest.spark.SparkPushService;



public class PostConfigureResource implements Resource {

	private DataHubBox box;
	private SparkManager<SparkPushService> manager;

	public PostConfigureResource(DataHubBox box, SparkManager manager) {
		this.box = box;
		this.manager = manager;

	}

	public void execute() throws BadRequest {fill(new io.intino.ness.datahub.box.actions.PostConfigureAction()).execute();
	}

	private io.intino.ness.datahub.box.actions.PostConfigureAction fill(io.intino.ness.datahub.box.actions.PostConfigureAction action) {
		action.box = this.box;
		action.context = context();
		action.name = manager.fromPath("name", String.class);
		action.configuration = manager.fromBody("configuration", String.class);
		action.attachment = manager.fromForm("attachment", io.intino.alexandria.Resource.class);
		return action;
	}

	private io.intino.alexandria.core.Context context() {
		io.intino.alexandria.core.Context context = new io.intino.alexandria.core.Context();
		context.put("domain", manager.domain());
		context.put("baseUrl", manager.baseUrl());
		context.put("requestUrl", manager.baseUrl() + manager.request().pathInfo());

		return context;
	}
}