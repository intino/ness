package io.intino.ness.box.jmx;

import io.intino.ness.box.NessBox;
import java.util.*;
import java.time.*;

public class Manager implements ManagerMBean {

	private final NessBox box;

	public Manager(NessBox box) {
		this.box = box;
	}

	public String addUser(String name, java.util.List<String> groups) {
		io.intino.ness.box.actions.AddUserAction action = new io.intino.ness.box.actions.AddUserAction();
		action.box = box;
		action.name = name;
	action.groups = groups;
		return action.execute();
	}

	public String removeUser(String name) {
		io.intino.ness.box.actions.RemoveUserAction action = new io.intino.ness.box.actions.RemoveUserAction();
		action.box = box;
		action.name = name;
		return action.execute();
	}

	public String addTank(String name) {
		io.intino.ness.box.actions.AddTankAction action = new io.intino.ness.box.actions.AddTankAction();
		action.box = box;
		action.name = name;
		return action.execute();
	}

	public String removeTank(String name) {
		io.intino.ness.box.actions.RemoveTankAction action = new io.intino.ness.box.actions.RemoveTankAction();
		action.box = box;
		action.name = name;
		return action.execute();
	}

	public java.util.List<String> users() {
		io.intino.ness.box.actions.UsersAction action = new io.intino.ness.box.actions.UsersAction();
		action.box = box;

		return action.execute();
	}

	public java.util.List<String> tanks(java.util.List<String> tags) {
		io.intino.ness.box.actions.TanksAction action = new io.intino.ness.box.actions.TanksAction();
		action.box = box;
		action.tags = tags;
		return action.execute();
	}

	public java.util.List<String> functions() {
		io.intino.ness.box.actions.FunctionsAction action = new io.intino.ness.box.actions.FunctionsAction();
		action.box = box;

		return action.execute();
	}

	public java.util.List<String> topics() {
		io.intino.ness.box.actions.TopicsAction action = new io.intino.ness.box.actions.TopicsAction();
		action.box = box;

		return action.execute();
	}

	public String rename(String tank, String name) {
		io.intino.ness.box.actions.RenameAction action = new io.intino.ness.box.actions.RenameAction();
		action.box = box;
		action.tank = tank;
	action.name = name;
		return action.execute();
	}

	public String seal(String tank) {
		io.intino.ness.box.actions.SealAction action = new io.intino.ness.box.actions.SealAction();
		action.box = box;
		action.tank = tank;
		return action.execute();
	}

	public String migrate(String tank, java.util.List<String> functions) {
		io.intino.ness.box.actions.MigrateAction action = new io.intino.ness.box.actions.MigrateAction();
		action.box = box;
		action.tank = tank;
	action.functions = functions;
		return action.execute();
	}

	public String reflow(java.util.List<String> tanks) {
		io.intino.ness.box.actions.ReflowAction action = new io.intino.ness.box.actions.ReflowAction();
		action.box = box;
		action.tanks = tanks;
		return action.execute();
	}

	public String addFunction(String name, String code) {
		io.intino.ness.box.actions.AddFunctionAction action = new io.intino.ness.box.actions.AddFunctionAction();
		action.box = box;
		action.name = name;
	action.code = code;
		return action.execute();
	}

	public String pump(String functionName, String input, String output) {
		io.intino.ness.box.actions.PumpAction action = new io.intino.ness.box.actions.PumpAction();
		action.box = box;
		action.functionName = functionName;
	action.input = input;
	action.output = output;
		return action.execute();
	}
}