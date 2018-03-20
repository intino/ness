package io.intino.ness.box.jmx;

import io.intino.konos.jmx.Description;
import io.intino.konos.jmx.Parameters;

import java.util.*;
import java.time.*;

public interface ManagerMBean {

	@Description("Shows information about the available operations")
	@Parameters({})
	java.util.List<String> help();

	@Description("Request all users registered in ness")
	@Parameters({})
	java.util.List<String> users();

	@Description("Add user to the datalake")
	@Parameters({"name"})
	String addUser(String name);

	@Description("Remove user from ness service")
	@Parameters({"name"})
	String removeUser(String name);

	@Description("Request all tanks nessy is subscribed filtering by tags")
	@Parameters({"tags"})
	java.util.List<String> tanks(java.util.List<String> tags);

	@Description("connect feed and flow channes of a tank")
	@Parameters({"tank"})
	String resumeTank(String tank);

	@Description("stops the feed and flow channes of a tank")
	@Parameters({"tank"})
	String pauseTank(String tank);

	@Description("Creates a tank")
	@Parameters({"name"})
	String addTank(String name);

	@Description("Removes registered tank")
	@Parameters({"name"})
	String removeTank(String name);

	@Description("Changes name of a tank for a new one")
	@Parameters({"tank", "name"})
	String renameTank(String tank, String name);

	@Description("Sort messages of all datalake tanks")
	@Parameters({})
	void sortTanks();

	@Description("Sort messages of a tank")
	@Parameters({"tank"})
	void sortTank(String tank);

	@Description("Connects source and destination topics")
	@Parameters({})
	String pipes();

	@Description("Connects source and destination topics real-time, optionaly converting the message with a function")
	@Parameters({"from", "to", "functionName"})
	String addPipe(String from, String to, String functionName);

	@Description("Removes a registered function")
	@Parameters({"origin"})
	String removePipes(String origin);

	@Description("Show registered topics")
	@Parameters({})
	java.util.List<String> topics();

	@Description("Removes registered topic")
	@Parameters({"topic"})
	Boolean removeTopic(String topic);

	@Description("Show all functions registered")
	@Parameters({})
	java.util.List<String> functions();

	@Description("Create a function associated to an input tank and output tank")
	@Parameters({"name", "code"})
	String addFunction(String name, String code);

	@Description("Removes a registered function")
	@Parameters({"name"})
	String removeFunction(String name);

	@Description("list of external buses")
	@Parameters({})
	java.util.List<String> externalBuses();

	@Description("Defines an external bus to interact with it using jms-connectors")
	@Parameters({"name", "externalBusUrl", "user", "password"})
	String addExternalBus(String name, String externalBusUrl, String user, String password);

	@Description("Removes a registed jms connector")
	@Parameters({"name"})
	String removeExternalBus(String name);

	@Description("list of jms connectors and its status")
	@Parameters({})
	java.util.List<String> jmsConnectors();

	@Description("Creates a data flow between an external bus and ness. It is necesary to define de direction of the data flow (*incoming* or *outgoing*) and topics separated by space. Also it is posible to set a conversion function.")
	@Parameters({"name", "externalBus", "direction", "topics"})
	String addJmsConnector(String name, String externalBus, String direction, String topics);

	@Description("Removes a registed jms connector")
	@Parameters({"name"})
	String removeJmsConnector(String name);

	@Description("connect out jms topic with a ness tank")
	@Parameters({"name"})
	String startJmsConnector(String name);

	@Description("connect out jms topic with a ness tank")
	@Parameters({"name"})
	String stopJmsConnector(String name);

	@Description("Connect a source and destination tanks through a `function`")
	@Parameters({"functionName", "input", "output"})
	String pump(String functionName, String input, String output);
}