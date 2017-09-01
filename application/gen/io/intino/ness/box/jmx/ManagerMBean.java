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

	@Description("Request all tanks nessy is subscribed filtering by tags")
	@Parameters({"tags"})
	java.util.List<String> tanks(java.util.List<String> tags);

	@Description("Show all functions registered")
	@Parameters({})
	java.util.List<String> functions();

	@Description("Show registered topics")
	@Parameters({})
	java.util.List<String> topics();

	@Description("list of aqueducts and its status")
	@Parameters({})
	java.util.List<String> aqueducts();

	@Description("Seals current events of a tank to reservoir")
	@Parameters({"tank"})
	String seal(String tank);

	@Description("Transforms events of a tank to a evolved tank")
	@Parameters({"tank", "functions"})
	String migrate(String tank, java.util.List<String> functions);

	@Description("Reproduce events of a list of tanks")
	@Parameters({"tanks"})
	String reflow(java.util.List<String> tanks);

	@Description("Connect a source and destination tanks through a `function`")
	@Parameters({"functionName", "input", "output"})
	String pump(String functionName, String input, String output);

	@Description("Connects source and destination topics")
	@Parameters({"from", "to"})
	String pipe(String from, String to);

	@Description("connect out jms topic with a ness tank")
	@Parameters({"name"})
	String startAqueduct(String name);

	@Description("connect out jms topic with a ness tank")
	@Parameters({"name"})
	String stopAqueduct(String name);

	@Description("connect feed and flow channes of a tank")
	@Parameters({"tank"})
	String resumeTank(String tank);

	@Description("stops the feed and flow channes of a tank")
	@Parameters({"tank"})
	String pauseTank(String tank);

	@Description("Add user to the datalake")
	@Parameters({"name"})
	String addUser(String name);

	@Description("Remove user from ness service")
	@Parameters({"name"})
	String removeUser(String name);

	@Description("Creates a tank")
	@Parameters({"name"})
	String addTank(String name);

	@Description("Removes registered tank")
	@Parameters({"name"})
	String removeTank(String name);

	@Description("Changes name of a tank for a new one")
	@Parameters({"tank", "name"})
	String renameTank(String tank, String name);

	@Description("Removes registered topic")
	@Parameters({"topic"})
	Boolean removeTopic(String topic);

	@Description("Create a function associated to an input tank and output tank")
	@Parameters({"name", "code"})
	String addFunction(String name, String code);

	@Description("Removes a registered function")
	@Parameters({"name"})
	String removeFunction(String name);

	@Description("Defines an external bus to interact with it using aqueducts")
	@Parameters({"name", "externalBusUrl", "user", "password"})
	String addExternalBus(String name, String externalBusUrl, String user, String password);

	@Description("Removes a registed aqueduct")
	@Parameters({"name"})
	String removeExternalBus(String name);

	@Description("Creates a data flow between an external bus and ness. It is necesary to define de direction of the data flow (*incoming* or *outgoing*). Also it is posible to set a conversion function.")
	@Parameters({"name", "externalBus", "direction", "functionName", "tankMacro"})
	String addAqueduct(String name, String externalBus, String direction, String functionName, String tankMacro);

	@Description("Removes a registed aqueduct")
	@Parameters({"name"})
	String removeAqueduct(String name);
}