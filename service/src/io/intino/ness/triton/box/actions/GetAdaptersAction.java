package io.intino.ness.triton.box.actions;

import io.intino.ness.triton.box.ServiceBox;

import java.util.List;
import java.util.stream.Collectors;


public class GetAdaptersAction {

	public ServiceBox box;
	public io.intino.alexandria.core.Context context = new io.intino.alexandria.core.Context();

	public List<String> execute() {
		return box.graph().adapterList().stream().map(a -> a.name$()).collect(Collectors.toList());
	}
}