package io.intino.ness.box.jmx;

import io.intino.ness.box.NessBox;
import io.intino.ness.box.actions.PauseTankAction;
import io.intino.ness.box.actions.ResumeTankAction;

import java.util.*;

public class Manager implements ManagerMBean {

	private final NessBox box;

	public java.util.List<String> help() {
		List<String> operations = new ArrayList<>();
		operations.addAll(java.util.Arrays.asList(new String[]{"java.util.List<String> users(): Request all users registered in ness", "java.util.List<String> tanks(java.util.List<String> tags): Request all tanks nessy is subscribed filtering by tags", "java.util.List<String> functions(): Show all functions registered", "java.util.List<String> topics(): Show registered topics", "java.util.List<String> aqueducts(): list of aqueducts and its status", "String seal(String tank): Seals current events of a tank to reservoir", "String migrate(String tank, java.util.List<String> functions): Transforms events of a tank to a evolved tank", "String reflow(java.util.List<String> tanks): Reproduce events of a tank", "String pump(String functionName, String input, String output): Connect a source and destination tanks through a `function`", "String startAqueduct(String name): connect out jms topic with a ness tank", "String stopAqueduct(String name): connect out jms topic with a ness tank", "String startFeedflow(String tank): connect feed and flow channes of a tank", "String stopFeedflow(String tank): stops the feed and flow channes of a tank", "String addUser(String name): Add user to the datalake", "String removeUser(String name): Remove user from ness service", "String addTank(String name): Creates a tank", "String removeTank(String name): Removes registered tank", "String renameTank(String tank, String name): Changes name of a tank for a new one", "Boolean removeTopic(String topic): Removes registered topic", "String addFunction(String name, String code): Create a function associated to an input tank and output tank", "String removeFunction(String name): Removes a registered function", "String addExternalBus(String name, String externalBusUrl, String user, String password): Defines an external bus to interact with it using aqueducts", "String removeExternalBus(String name): Removes a registed aqueduct", "String addAqueduct(String name, String externalBus, String direction, String functionName, String tankMacro): Creates a data flow between an external bus and ness. It is necesary to define de direction of the data flow (*incoming* or *outgoing*). Also it is posible to set a conversion function.", "String removeAqueduct(String name): Removes a registed aqueduct"}));
		return operations;
	}

	public Manager(NessBox box) {
		this.box = box;
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

	public java.util.List<String> aqueducts() {
		io.intino.ness.box.actions.AqueductsAction action = new io.intino.ness.box.actions.AqueductsAction();
		action.box = box;

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

	public String pump(String functionName, String input, String output) {
		io.intino.ness.box.actions.PumpAction action = new io.intino.ness.box.actions.PumpAction();
		action.box = box;
		action.functionName = functionName;
	action.input = input;
	action.output = output;
		return action.execute();
	}

	public String startAqueduct(String name) {
		io.intino.ness.box.actions.StartAqueductAction action = new io.intino.ness.box.actions.StartAqueductAction();
		action.box = box;
		action.name = name;
		return action.execute();
	}

	public String stopAqueduct(String name) {
		io.intino.ness.box.actions.StopAqueductAction action = new io.intino.ness.box.actions.StopAqueductAction();
		action.box = box;
		action.name = name;
		return action.execute();
	}

	public String startFeedflow(String tank) {
		ResumeTankAction action = new ResumeTankAction();
		action.box = box;
		action.tank = tank;
		return action.execute();
	}

	public String stopFeedflow(String tank) {
		PauseTankAction action = new PauseTankAction();
		action.box = box;
		action.tank = tank;
		return action.execute();
	}

	public String addUser(String name) {
		io.intino.ness.box.actions.AddUserAction action = new io.intino.ness.box.actions.AddUserAction();
		action.box = box;
		action.name = name;
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

	public String renameTank(String tank, String name) {
		io.intino.ness.box.actions.RenameTankAction action = new io.intino.ness.box.actions.RenameTankAction();
		action.box = box;
		action.tank = tank;
	action.name = name;
		return action.execute();
	}

	public Boolean removeTopic(String topic) {
		io.intino.ness.box.actions.RemoveTopicAction action = new io.intino.ness.box.actions.RemoveTopicAction();
		action.box = box;
		action.topic = topic;
		return action.execute();
	}

	public String addFunction(String name, String code) {
		io.intino.ness.box.actions.AddFunctionAction action = new io.intino.ness.box.actions.AddFunctionAction();
		action.box = box;
		action.name = name;
	action.code = code;
		return action.execute();
	}

	public String removeFunction(String name) {
		io.intino.ness.box.actions.RemoveFunctionAction action = new io.intino.ness.box.actions.RemoveFunctionAction();
		action.box = box;
		action.name = name;
		return action.execute();
	}

	public String addExternalBus(String name, String externalBusUrl, String user, String password) {
		io.intino.ness.box.actions.AddExternalBusAction action = new io.intino.ness.box.actions.AddExternalBusAction();
		action.box = box;
		action.name = name;
	action.externalBusUrl = externalBusUrl;
	action.user = user;
	action.password = password;
		return action.execute();
	}

	public String removeExternalBus(String name) {
		io.intino.ness.box.actions.RemoveExternalBusAction action = new io.intino.ness.box.actions.RemoveExternalBusAction();
		action.box = box;
		action.name = name;
		return action.execute();
	}

	public String addAqueduct(String name, String externalBus, String direction, String functionName, String tankMacro) {
		io.intino.ness.box.actions.AddAqueductAction action = new io.intino.ness.box.actions.AddAqueductAction();
		action.box = box;
		action.name = name;
	action.externalBus = externalBus;
	action.direction = direction;
	action.functionName = functionName;
	action.tankMacro = tankMacro;
		return action.execute();
	}

	public String removeAqueduct(String name) {
		io.intino.ness.box.actions.RemoveAqueductAction action = new io.intino.ness.box.actions.RemoveAqueductAction();
		action.box = box;
		action.name = name;
		return action.execute();
	}
}