package io.intino.ness.box;

import java.util.LinkedHashMap;
import java.util.logging.Logger;
import java.util.Map;
import java.util.UUID;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;


public abstract class AbstractBox extends io.intino.konos.Box {
	private static Logger LOG = Logger.getGlobal();
	protected NessConfiguration configuration;
	private io.intino.konos.jmx.JMXServer manager;
	private io.intino.konos.slack.Bot nessie;
	private io.intino.konos.scheduling.KonosTasker tasker = new io.intino.konos.scheduling.KonosTasker();

	public AbstractBox(String[] args) {
		this(new NessConfiguration(args));
	}

	public AbstractBox(NessConfiguration configuration) {

		initLogger();
		this.configuration = configuration;


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

	public io.intino.konos.Box open() {
		if(owner != null) owner.open();
		initActivities();
		initRESTServices();
		initJMXServices();
		initJMSServices();
		initDataLake();
		initTasks();
		initSlackBots();
		return this;
	}

	public void close() {
		if(owner != null) owner.close();



	}

	private void initRESTServices() {


	}

	private void initJMSServices() {



	}

	private void initJMXServices() {
		this.manager = new JMXManager().init(((NessBox) this));

	}

	private void initSlackBots() {

		if (configuration().nessieConfiguration == null) return;
		this.nessie = new NessieSlackBot((NessBox) this);
	}

	private void initActivities() {

	}

	private void initDataLake() {

	}

	private void initTasks() {
		Tasks.init(this.tasker, (NessBox) this);
	}

	private void initLogger() {
		final Logger logger = Logger.getGlobal();
		final ConsoleHandler handler = new ConsoleHandler();
		handler.setLevel(Level.INFO);
		handler.setFormatter(new io.intino.konos.LogFormatter("log"));
		logger.addHandler(handler);
	}
}