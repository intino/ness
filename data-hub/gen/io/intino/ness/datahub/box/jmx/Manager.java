package io.intino.ness.datahub.box.jmx;

import io.intino.ness.datahub.box.DataHubBox;
import java.util.*;
import java.time.*;

public class Manager implements ManagerMBean {

	private final DataHubBox box;

	public java.util.List<String> help() {
		List<String> operations = new ArrayList<>();
		operations.addAll(java.util.Arrays.asList(new String[]{"void seal(): Seal stage", "void stop(): Stops datalake service saving current information"}));
		return operations;
	}

	public Manager(DataHubBox box) {
		this.box = box;
	}

	public void seal() {
		io.intino.ness.datahub.box.actions.SealAction action = new io.intino.ness.datahub.box.actions.SealAction();
		action.box = box;action.execute();
	}

	public void stop() {
		io.intino.ness.datahub.box.actions.StopAction action = new io.intino.ness.datahub.box.actions.StopAction();
		action.box = box;action.execute();
	}
}