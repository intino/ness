package io.intino.ness.datalake.konos;

import java.util.LinkedHashMap;
import java.util.logging.Logger;
import java.util.Map;
import java.util.UUID;



public class DatalakeBox extends io.intino.konos.Box {
	private static Logger LOG = Logger.getGlobal();
	protected DatalakeConfiguration configuration;
	private io.intino.ness.datalake.konos.NessBus nessBus;



	public DatalakeBox(DatalakeConfiguration configuration) {

		configuration.args().entrySet().forEach((e) -> box.put(e.getKey(), e.getValue()));
		this.configuration = configuration;
	}


	public DatalakeConfiguration configuration() {
		return (DatalakeConfiguration) configuration;
	}


	public io.intino.ness.datalake.konos.NessBus nessBus() {
		return nessBus;
	}



	public void init() { 
		initActivities();
		initRESTServices();
		initJMXServices();
		initJMSServices();
		initBuses();
		initTasks();
		initSlackBots();
	}

	void quit() {

		nessBus.closeSession();
	}

	private void initRESTServices() {

	}

	private void initJMSServices() {


	}

	private void initJMXServices() {

	}

	private void initSlackBots() {

	}

	private void initActivities() {

	}

	private void initBuses() {
		this.nessBus = new io.intino.ness.datalake.konos.NessBus(this);
	}

	private void initTasks() {

	}

	public void stopJMSServices() {

	}

}