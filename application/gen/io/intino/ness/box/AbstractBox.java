package io.intino.ness.box;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import static org.slf4j.Logger.ROOT_LOGGER_NAME;


public abstract class AbstractBox extends io.intino.konos.Box {
	private static Logger logger = LoggerFactory.getLogger(ROOT_LOGGER_NAME);
	protected NessConfiguration configuration;
	private io.intino.konos.jmx.JMXServer manager;
	private io.intino.konos.slack.Bot nessie;
	private io.intino.konos.scheduling.KonosTasker tasker = new io.intino.konos.scheduling.KonosTasker();

	public AbstractBox(String[] args) {
		this(new NessConfiguration(args));
	}

	public AbstractBox(NessConfiguration configuration) {

		this.configuration = configuration;
		initLogger();


	}

	public NessConfiguration configuration() {
		return (NessConfiguration) configuration;
	}

	@Override
	public io.intino.konos.Box put(Object o) {

		return this;
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



public NessieSlackBot nessie() {
	return (NessieSlackBot) nessie;
}



	public io.intino.konos.scheduling.KonosTasker tasker() {
		return this.tasker;
	}

	private void initRESTServices() {


	}

	private void initJMSServices() {



	}

	private void initJMXServices() {
		this.manager = new JMXManager().init(((NessBox) this));
		logger.info("JMX service Manager: started!");

	}

	private void initSlackBots() {

		if (configuration().nessieConfiguration == null) return;
		this.nessie = new NessieSlackBot((NessBox) this);
		logger.info("Slack service Nessie: started!");
	}

	private void initActivities() {

	}

	private void initDataLake() {

	}

	private void initTasks() {
		Tasks.init(this.tasker, (NessBox) this);
	}

	private void initLogger() {
		final java.util.logging.Logger logger = java.util.logging.Logger.getGlobal();
		final ConsoleHandler handler = new ConsoleHandler();
		handler.setLevel(Level.INFO);
		handler.setFormatter(new io.intino.konos.LogFormatter("log"));
		logger.addHandler(handler);
	}
}