package io.intino.ness.datahub.box.actions;

import io.intino.alexandria.exceptions.BadRequest;
import io.intino.ness.datahub.box.DataHubBox;


public class PostConfigureAction {

	public DataHubBox box;
	public io.intino.alexandria.core.Context context = new io.intino.alexandria.core.Context();
	public String name;
	public String configuration;
	public io.intino.alexandria.Resource attachment;

	public void execute() throws BadRequest {

	}
}