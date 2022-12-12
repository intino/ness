package io.intino.datahub.box.actions;

import io.intino.datahub.box.DataHubBox;

import java.io.File;
import java.util.Arrays;
import java.util.stream.Collectors;


public class MappersAction {
	public DataHubBox box;
	public io.intino.alexandria.Context context = new io.intino.alexandria.Context();

	public String execute() {
		File[] files = box.mappersDirectory().listFiles(f -> f.getName().endsWith(".java"));
		if (files != null) {
			String collect = Arrays.stream(files).map(f -> f.getName().replace(".java", "")).collect(Collectors.joining("\n"));
			if (!collect.isEmpty()) return collect;
		}
		return "There is no mappers yet";
	}
}