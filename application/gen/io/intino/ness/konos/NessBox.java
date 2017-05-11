package io.intino.ness.konos;

import java.util.LinkedHashMap;
import java.util.logging.Logger;
import java.util.Map;
import java.util.UUID;

import io.intino.tara.magritte.Graph;

public class NessBox extends io.intino.konos.Box {
	private static Logger LOG = Logger.getGlobal();
	private Tuner tuner;
	protected NessConfiguration configuration;
	private io.intino.konos.jmx.JMXServer manager;
	private io.intino.konos.slack.Bot nessie;
	private io.intino.konos.scheduling.KonosTasker tasker = new io.intino.konos.scheduling.KonosTasker();

	protected String graphID;

	public NessBox(String[] args) {
		this(new NessConfiguration(args));
	}

	public NessBox(NessConfiguration configuration) {

		this.tuner = new Tuner(configuration);
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



	public io.intino.konos.scheduling.KonosTasker tasker() {
		return this.tasker;
	}

	public NessBox open() {
		graphID = UUID.randomUUID().toString();
		box.put(graphID, tuner.initGraph());
		init();
		start();
		return this;
	}

	private void start() {
		tuner.start(this);
	}

	public void init() { 
		initActivities();
		initRESTServices();
		initJMXServices();
		initJMSServices();
		initDataLake();
		initTasks();
		initSlackBots();
	}

	void close() {
		tuner.terminate(this);



	}

	private void initRESTServices() {


	}

	private void initJMSServices() {



	}

	private void initJMXServices() {
		this.manager = new JMXManager().init(this);

	}

	private void initSlackBots() {

		if (configuration().nessieConfiguration == null) return;
		this.nessie = new NessieSlackBot(this);
	}

	private void initActivities() {

	}

	private void initDataLake() {

	}

	private void initTasks() {
		Tasks.init(this.tasker, this);
	}

	public void stopJMSServices() {

	}
}