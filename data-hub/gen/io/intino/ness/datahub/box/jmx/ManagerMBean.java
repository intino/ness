package io.intino.ness.datahub.box.jmx;

import io.intino.alexandria.jmx.Description;
import io.intino.alexandria.jmx.Parameters;

import java.util.*;
import java.time.*;

public interface ManagerMBean {

	@Description("Shows information about the available operations")
	@Parameters({})
	java.util.List<String> help();

	@Description("Seal stage")
	@Parameters({})
	void seal();

	@Description("Stops datalake service saving current information")
	@Parameters({})
	void stop();
}