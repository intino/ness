package io.intino.ness.box.jmx;

import io.intino.ness.box.NessBox;
import io.intino.ness.box.actions.BusPipesAction;
import io.intino.ness.box.actions.StartBusPipeAction;
import io.intino.ness.box.actions.StopBusPipeAction;

import java.util.*;

public class Manager implements ManagerMBean {

	private final NessBox box;

	public java.util.List<String> help() {
		List<String> operations = new ArrayList<>();
		operations.addAll(java.util.Arrays.asList(new String[]{"java.util.List<String> users(): Request all users registered in ness", "String addUser(String name): Add user to the datalake", "String removeUser(String name): Remove user from ness service", "java.util.List<String> tanks(java.util.List<String> tags): Request all tanks nessy is subscribed filtering by tags", "String resumeTank(String tank): connect feed and flow channes of a tank", "String pauseTank(String tank): stops the feed and flow channes of a tank", "String addTank(String name): Creates a tank", "String removeTank(String name): Removes registered tank", "String renameTank(String tank, String name): Changes name of a tank for a new one", "String pipes(): Connects source and destination topics", "String addPipe(String from, String to, String functionName): Connects source and destination topics real-time, optionaly converting the message with a function", "String removePipes(String origin): Removes a registered function", "java.util.List<String> topics(): Show registered topics", "Boolean removeTopic(String topic): Removes registered topic", "java.util.List<String> functions(): Show all functions registered", "String addFunction(String name, String code): Create a function associated to an input tank and output tank", "String removeFunction(String name): Removes a registered function", "java.util.List<String> externalBuses(): list of external buses", "String addExternalBus(String name, String externalBusUrl, String user, String password): Defines an external bus to interact with it using aqueducts", "String removeExternalBus(String name): Removes a registed aqueduct", "java.util.List<String> aqueducts(): list of aqueducts and its status", "String addAqueduct(String name, String externalBus, String direction, String functionName, String tankMacro): Creates a data flow between an external bus and ness. It is necesary to define de direction of the data flow (*incoming* or *outgoing*). Also it is posible to set a conversion function.", "String removeAqueduct(String name): Removes a registed aqueduct", "String startAqueduct(String name): connect out jms topic with a ness tank", "String stopAqueduct(String name): connect out jms topic with a ness tank", "String seal(String tank): Seals current events of a tank to reservoir", "String migrate(String tank, java.util.List<String> functions): Transforms events of a tank to a evolved tank", "String pump(String functionName, String input, String output): Connect a source and destination tanks through a `function`"}));
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

	public java.util.List<String> tanks(java.util.List<String> tags) {
		io.intino.ness.box.actions.TanksAction action = new io.intino.ness.box.actions.TanksAction();
		action.box = box;
		action.tags = tags;
		return action.execute();
	}

	public String resumeTank(String tank) {
		io.intino.ness.box.actions.ResumeTankAction action = new io.intino.ness.box.actions.ResumeTankAction();
		action.box = box;
		action.tank = tank;
		return action.execute();
	}

	public String pauseTank(String tank) {
		io.intino.ness.box.actions.PauseTankAction action = new io.intino.ness.box.actions.PauseTankAction();
		action.box = box;
		action.tank = tank;
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

	public String pipes() {
		io.intino.ness.box.actions.PipesAction action = new io.intino.ness.box.actions.PipesAction();
		action.box = box;

		return action.execute();
	}

	public String addPipe(String from, String to, String functionName) {
		io.intino.ness.box.actions.AddPipeAction action = new io.intino.ness.box.actions.AddPipeAction();
		action.box = box;
		action.from = from;
	action.to = to;
	action.functionName = functionName;
		return action.execute();
	}

	public String removePipes(String origin) {
		io.intino.ness.box.actions.RemovePipesAction action = new io.intino.ness.box.actions.RemovePipesAction();
		action.box = box;
		action.origin = origin;
		return action.execute();
	}

	public java.util.List<String> topics() {
		io.intino.ness.box.actions.TopicsAction action = new io.intino.ness.box.actions.TopicsAction();
		action.box = box;

		return action.execute();
	}

	public Boolean removeTopic(String topic) {
		io.intino.ness.box.actions.RemoveTopicAction action = new io.intino.ness.box.actions.RemoveTopicAction();
		action.box = box;
		action.topic = topic;
		return action.execute();
	}

	public java.util.List<String> functions() {
		io.intino.ness.box.actions.FunctionsAction action = new io.intino.ness.box.actions.FunctionsAction();
		action.box = box;

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

	public java.util.List<String> externalBuses() {
		io.intino.ness.box.actions.ExternalBusesAction action = new io.intino.ness.box.actions.ExternalBusesAction();
		action.box = box;

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

	public java.util.List<String> aqueducts() {
		BusPipesAction action = new BusPipesAction();
		action.box = box;

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

	public String startAqueduct(String name) {
		StartBusPipeAction action = new StartBusPipeAction();
		action.box = box;
		action.name = name;
		return action.execute();
	}

	public String stopAqueduct(String name) {
		StopBusPipeAction action = new StopBusPipeAction();
		action.box = box;
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

	public String pump(String functionName, String input, String output) {
		io.intino.ness.box.actions.PumpAction action = new io.intino.ness.box.actions.PumpAction();
		action.box = box;
		action.functionName = functionName;
	action.input = input;
	action.output = output;
		return action.execute();
	}
}