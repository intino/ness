package io.intino.ness.datahub.box;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import io.intino.alexandria.logger.Logger;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;

public abstract class AbstractBox extends io.intino.alexandria.core.Box {
	protected DataHubConfiguration configuration;
	private io.intino.alexandria.jmx.JMXServer manager;
	private io.intino.alexandria.scheduler.AlexandriaScheduler scheduler = new io.intino.alexandria.scheduler.AlexandriaScheduler();

	public AbstractBox(String[] args) {
		this(new DataHubConfiguration(args));
	}

	public AbstractBox(DataHubConfiguration configuration) {
		this.configuration = configuration;
		initJavaLogger();
		io.intino.alexandria.rest.AlexandriaSparkBuilder.setup(Integer.parseInt(configuration().get("api_port")), "www/");
	}

	public DataHubConfiguration configuration() {
		return configuration;
	}

	@Override
	public io.intino.alexandria.core.Box put(Object o) {
		return this;
	}

	public io.intino.alexandria.core.Box open() {
		if (owner != null) owner.open();
		initUI();
		initRESTServices();
		initJMXServices();
		initJMSServices();
		initDatalake();
		initMessageHub();
		initTasks();
		initSlackBots();
		return this;
	}

	public void close() {
		if (owner != null) owner.close();
		io.intino.alexandria.rest.AlexandriaSparkBuilder.instance().stop();
	}

	public io.intino.alexandria.scheduler.AlexandriaScheduler scheduler() {
		return this.scheduler;
	}


	private void initRESTServices() {
		ApiService.setup(io.intino.alexandria.rest.AlexandriaSparkBuilder.instance(), (DataHubBox) this).start();
		Logger.info("REST service api: started!");
	}

	private void initJMSServices() {

	}

	private void initJMXServices() {
		this.manager = new JMXManager().init(((DataHubBox) this));
		Logger.info("JMX service Manager: started!");
	}

	private void initSlackBots() {

	}

	private void initUI() {

	}

	private void initDatalake() {
	}

	private void initMessageHub() {
	}

	private void initTasks() {
		Tasks.init(this.scheduler, (DataHubBox) this);
	}

	private void initJavaLogger() {
		final java.util.logging.Logger Logger = java.util.logging.Logger.getGlobal();
		final ConsoleHandler handler = new ConsoleHandler();
		handler.setLevel(Level.INFO);
		handler.setFormatter(new io.intino.alexandria.logger.Formatter());
		Logger.setUseParentHandlers(false);
		Logger.addHandler(handler);
	}

	private java.net.URL url(String url) {
		try {
			return new java.net.URL(url);
		} catch (java.net.MalformedURLException e) {
			return null;
		}
	}
}