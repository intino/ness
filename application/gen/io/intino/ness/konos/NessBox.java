package io.intino.ness.konos;

import java.util.LinkedHashMap;
import java.util.logging.Logger;
import java.util.Map;
import java.util.UUID;

import io.intino.tara.magritte.Graph;

public class NessBox extends io.intino.konos.Box {
	private static Logger LOG = Logger.getGlobal();
	protected NessConfiguration configuration;
	private io.intino.konos.slack.Bot nessie;

	private String graphID;

	public NessBox(io.intino.tara.magritte.Graph graph, NessConfiguration configuration) {
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

	public NessConfiguration configuration() {
		return (NessConfiguration) configuration;
	}

	public NessieSlackBot nessie() {
		return (NessieSlackBot) nessie;
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


	}

	private void initRESTServices() {

	}

	private void initJMSServices() {


	}

	private void initJMXServices() {

	}

	private void initSlackBots() {
		if (configuration().nessieConfiguration == null) return;
		this.nessie = new NessieSlackBot(this);
	}

	private void initActivities() {

	}

	private void initBuses() {

	}

	private void initTasks() {

	}

	public void stopJMSServices() {

	}

}