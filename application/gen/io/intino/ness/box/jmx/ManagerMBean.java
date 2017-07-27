package io.intino.ness.box.jmx;

import io.intino.konos.jmx.Description;
import io.intino.konos.jmx.Parameters;

import java.util.*;
import java.time.*;

public interface ManagerMBean {

	@Description("Add user to the datalake")
	@Parameters({"name", "groups"})
	String addUser(String name, java.util.List<String> groups);

	@Description("Remove user from ness service")
	@Parameters({"name"})
	String removeUser(String name);

	@Description("Creates a tank")
	@Parameters({"name"})
	String addTank(String name);

	@Description("Removes registered tank")
	@Parameters({"name"})
	String removeTank(String name);

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

	@Description("Changes name of a tank for a new one")
	@Parameters({"tank", "name"})
	String rename(String tank, String name);

	@Description("Seals current events of a tank to reservoir")
	@Parameters({"tank"})
	String seal(String tank);

	@Description("Transforms events of a tank to a evolved tank")
	@Parameters({"tank", "functions"})
	String migrate(String tank, java.util.List<String> functions);

	@Description("Reproduce events of a tank")
	@Parameters({"tanks"})
	String reflow(java.util.List<String> tanks);

	@Description("Create a function associated to an input tank and output tank")
	@Parameters({"name", "code"})
	String addFunction(String name, String code);

	@Description("Connect a source and destination tanks through a `function`")
	@Parameters({"functionName", "input", "output"})
	String pump(String functionName, String input, String output);
}