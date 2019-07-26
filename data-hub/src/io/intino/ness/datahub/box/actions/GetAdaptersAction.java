package io.intino.ness.datahub.box.actions;

import io.intino.ness.datahub.box.DataHubBox;
import io.intino.tara.magritte.Layer;

import java.util.List;
import java.util.stream.Collectors;


public class GetAdaptersAction {

	public DataHubBox box;
	public io.intino.alexandria.core.Context context = new io.intino.alexandria.core.Context();

	public List<String> execute() {
		return box.graph().adapterList().stream().map(Layer::name$).collect(Collectors.toList());
	}
}