package org.example.test.datamart;

import groovy.lang.GroovyShell;
import io.intino.alexandria.event.message.MessageEvent;
import io.intino.alexandria.message.Message;
import io.intino.alexandria.terminal.JmsConnector;
import io.intino.test.datahubtest.TestTerminal;
import io.intino.test.datahubtest.datamarts.master.MasterDatamart;
import io.intino.test.datahubtest.datamarts.master.entities.JavaApplication;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.time.Instant;

import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

public class DatamartImplTest {

	static MessageEvent event = new MessageEvent(new Message("UserAssertion")
			.set("ss", "test")
			.set("ts", Instant.now())
			.set("id", "user1")
			.set("name", "Cristian")
			.set("language", "es"));

	public static void main(String[] args) throws IOException {
		TestTerminal terminal = new TestTerminal(connector());
		MasterDatamart datamart = terminal.masterDatamart();

		System.out.println(JavaApplication.definition.descendants());

		datamart.observable("hola");

		var def = JavaApplication.definition;
		System.out.println(def.attributes());
		System.out.println(def.declaredAttributes());
	}

	private static void launchWatchService(TestTerminal terminal) {
		new Thread(() -> {
			try {
				File script = new File("C:\\Users\\naits\\Desktop\\IntinoDev\\ness\\test\\test\\org\\example\\test\\datamart\\DatamartScript.groovy");
				GroovyShell shell = new GroovyShell();
				shell.setProperty("Terminal", terminal);
				shell.setProperty("Datamart", terminal.masterDatamart());

				WatchService watchService = FileSystems.getDefault().newWatchService();
				script.getParentFile().toPath().register(watchService, ENTRY_MODIFY);

				try {
					shell.evaluate(script);
				} catch (Throwable e) {
					e.printStackTrace();
				}
				System.out.println("\n\n");

				WatchKey key;
				long last = System.currentTimeMillis();
				while((key = watchService.take()) != null) {
					for(var event : key.pollEvents()) {
						if(event.context().toString().endsWith(".groovy")) {
							if(System.currentTimeMillis() - last < 2000) continue;
							last = System.currentTimeMillis();
							try {
								shell.evaluate(script);
								break;
							} catch (Throwable e) {
								e.printStackTrace();
							} finally {
								System.out.println("\n\n");
							}
						}
					}
					key.reset();
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}).start();
	}

	private static JmsConnector connector() {
		JmsConnector jmsConnector = new JmsConnector(
				new io.intino.alexandria.jms.ConnectionConfig("failover:(tcp://localhost:63000)", "test", "test", "test"),
				new File("temp/cache")
		);
		jmsConnector.start();
		return jmsConnector;
	}
}
