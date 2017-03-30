package io.intino.ness.konos;

import java.util.LinkedHashMap;
import java.util.logging.Logger;
import java.util.Map;
import java.util.UUID;

import io.intino.tara.magritte.Graph;

public class ApplicationBox extends io.intino.konos.Box {
	private static Logger LOG = Logger.getGlobal();
	protected ApplicationConfiguration configuration;
	private io.intino.ness.konos.NessBus nessBus;

	private String graphID;

	public ApplicationBox(io.intino.tara.magritte.Graph graph, ApplicationConfiguration configuration) {
		box.put(graphID = UUID.randomUUID().toString(), graph);
		configuration.args().entrySet().forEach((e) -> box.put(e.getKey(), e.getValue()));
		this.configuration = configuration;
	}

	public io.intino.tara.magritte.Graph graph() {
		return (io.intino.tara.magritte.Graph) box().get(graphID);
	}

	public void graph(io.intino.tara.magritte.Graph graph) {
		box().put(graphID, graph);
	}

	public ApplicationConfiguration configuration() {
		return (ApplicationConfiguration) configuration;
	}


	public io.intino.ness.konos.NessBus nessBus() {
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
		this.nessBus = new io.intino.ness.konos.NessBus(this);
	}

	private void initTasks() {

	}

	public void stopJMSServices() {

	}

}