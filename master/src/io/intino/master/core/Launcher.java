package io.intino.master.core;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;

import static java.util.stream.Collectors.toMap;

public class Launcher {

	private Function<MasterConfig, Master> masterImpl = Master::new;
	private Runnable loggerConfigurator = this::configureLogger;

	public void launch(String[] args) {
		Map<String, String> arguments = asMap(args);
		configureLogger();
		Master master = masterImpl.apply(new MasterConfig(arguments));
		master.start();
	}

	private void configureLogger() {
		java.util.logging.Logger rootLogger = LogManager.getLogManager().getLogger("");
		rootLogger.setLevel(Level.WARNING);
		for (Handler h : rootLogger.getHandlers()) rootLogger.removeHandler(h);
		final ConsoleHandler handler = new ConsoleHandler();
		handler.setLevel(Level.WARNING);
		handler.setFormatter(new io.intino.alexandria.logger.Formatter());
		rootLogger.setUseParentHandlers(false);
		rootLogger.addHandler(handler);
	}

	private Map<String, String> asMap(String[] args) {
		return Arrays.stream(args)
				.map(a -> a.split("="))
				.collect(toMap(s -> s[0].trim(), s -> s[1].trim()));
	}

	public Launcher setMasterImpl(Function<MasterConfig, Master> masterImpl) {
		this.masterImpl = masterImpl == null ? Master::new : masterImpl;
		return this;
	}
}
