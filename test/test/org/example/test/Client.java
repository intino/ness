package org.example.test;

import io.intino.alexandria.jms.ConnectionConfig;
import io.intino.alexandria.jms.JmsProducer;
import io.intino.alexandria.jms.QueueProducer;
import io.intino.alexandria.logger.Logger;
import io.intino.alexandria.terminal.JmsConnector;
import io.intino.test.datahubtest.TestTerminal;
import io.intino.test.datahubtest.datamarts.master.MasterDatamart;
import io.intino.test.datahubtest.datamarts.master.MasterDatamartImpl;
import io.intino.test.datahubtest.messages.inventory.JavaApplicationAssertion;
import org.apache.activemq.command.ActiveMQTextMessage;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageNotWriteableException;
import javax.jms.TemporaryQueue;
import java.io.File;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class Client {

	public static void main(String[] args) throws Exception {
		if(true || args[0].equals("test")) {
			main2(args);
			return;
		}

		int count = 0;
		String user = args[0];
		String password = args[1];
		String clientId = args[2];

		JmsConnector connector = connector(user, password, clientId);

		ActiveMQTextMessage message = new ActiveMQTextMessage();
		message.setText("datamart:master:");

		while(true) {
			Message m = connector.requestResponse("service.ness.datalake.messagestore", message, 5, TimeUnit.SECONDS);
			System.out.println(m);
			Thread.sleep(2000);
		}
	}

	public static void main2(String[] args) throws InterruptedException {
		int count = 0;
		String user = args[0];
		String password = args[1];
		String clientId = args[2];

		JmsConnector connector = connector(user, password, clientId);
		TestTerminal terminal = new TestTerminal(connector);
		System.out.println(user + ": Initializing datamarts...");
		terminal.initDatamarts();
//
		MasterDatamart datamart = terminal.masterDatamart();

//		addSubscribers(connector, terminal, (MasterDatamartImpl) datamart);
//		((MasterDatamartImpl)datamart).init();

		System.out.println(user + ": Datamarts initialized");


		while(true) {
			System.out.println("=============\n\n");
			Thread.sleep(3000);

			datamart.entities().forEach(System.out::println);
			System.out.println();
			System.out.println(user + ": Datamart size = " + datamart.size() + " entities");

			JavaApplicationAssertion event = new JavaApplicationAssertion(user, "app-" + user + "-" + ++count);
			event.name(user + System.currentTimeMillis());
			event.classpath(List.of(String.valueOf(System.currentTimeMillis())));
			event.debugPort(new Random().nextInt(65000));
			event.jmxPort(new Random().nextInt(65000));
			event.maxMemory(new Random().nextInt(65000));

			terminal.publish(event);

			System.out.println(user + ": Event " + event.id() + " published");
		}
	}

	private static void addSubscribers(JmsConnector connector, TestTerminal terminal, MasterDatamartImpl datamart) {
		terminal.subscribe((TestTerminal.UserAssertionConsumer) (event, topic) -> datamart.init().mount(event), connector.clientId() + "_master_UserAssertion");
		terminal.subscribe((TestTerminal.TeamAssertionConsumer) (event, topic) -> datamart.init().mount(event), connector.clientId() + "_master_TeamAssertion");
		terminal.subscribe((TestTerminal.ChannelAssertionConsumer) (event, topic) -> datamart.init().mount(event), connector.clientId() + "_master_ChannelAssertion");
		terminal.subscribe((TestTerminal.InventoryAreaAssertionConsumer) (event, topic) -> datamart.init().mount(event), connector.clientId() + "_master_AreaAssertion");
		terminal.subscribe((TestTerminal.InventoryAssetAssertionConsumer) (event, topic) -> datamart.init().mount(event), connector.clientId() + "_master_AssetAssertion");
		terminal.subscribe((TestTerminal.InventoryHardwareAssertionConsumer) (event, topic) -> datamart.init().mount(event), connector.clientId() + "_master_HardwareAssertion");
		terminal.subscribe((TestTerminal.InventoryPeripheralDeviceAssertionConsumer) (event, topic) -> datamart.init().mount(event), connector.clientId() + "_master_PeripheralDeviceAssertion");
		terminal.subscribe((TestTerminal.InventoryNetworkDeviceAssertionConsumer) (event, topic) -> datamart.init().mount(event), connector.clientId() + "_master_NetworkDeviceAssertion");
		terminal.subscribe((TestTerminal.InventoryMachineAssertionConsumer) (event, topic) -> datamart.init().mount(event), connector.clientId() + "_master_MachineAssertion");
		terminal.subscribe((TestTerminal.InventorySoftwareAssertionConsumer) (event, topic) -> datamart.init().mount(event), connector.clientId() + "_master_SoftwareAssertion");
		terminal.subscribe((TestTerminal.InventoryConsulAssertionConsumer) (event, topic) -> datamart.init().mount(event), connector.clientId() + "_master_ConsulAssertion");
		terminal.subscribe((TestTerminal.InventoryServiceAssertionConsumer) (event, topic) -> datamart.init().mount(event), connector.clientId() + "_master_ServiceAssertion");
		terminal.subscribe((TestTerminal.InventoryRdbmsServiceAssertionConsumer) (event, topic) -> datamart.init().mount(event), connector.clientId() + "_master_RdbmsServiceAssertion");
		terminal.subscribe((TestTerminal.InventoryQueryAssertionConsumer) (event, topic) -> datamart.init().mount(event), connector.clientId() + "_master_QueryAssertion");
		terminal.subscribe((TestTerminal.InventoryApplicationAssertionConsumer) (event, topic) -> datamart.init().mount(event), connector.clientId() + "_master_ApplicationAssertion");
		terminal.subscribe((TestTerminal.InventoryJavaApplicationAssertionConsumer) (event, topic) -> datamart.init().mount(event), connector.clientId() + "_master_JavaApplicationAssertion");
		terminal.subscribe((TestTerminal.InventoryPersonAssertionConsumer) (event, topic) -> datamart.init().mount(event), connector.clientId() + "_master_PersonAssertion");
		terminal.subscribe((TestTerminal.InventoryBusinessUnitAssertionConsumer) (event, topic) -> datamart.init().mount(event), connector.clientId() + "_master_BusinessUnitAssertion");
		terminal.subscribe((TestTerminal.MonitoringAnomalyTypeAssertionConsumer) (event, topic) -> datamart.init().mount(event), connector.clientId() + "_master_AnomalyTypeAssertion");
		terminal.subscribe((TestTerminal.MonitoringAnomalyRuleAssertionConsumer) (event, topic) -> datamart.init().mount(event), connector.clientId() + "_master_AnomalyRuleAssertion");
	}

	private static JmsConnector connector(String user, String password, String clientId) {
		JmsConnector jmsConnector = new MyJmsConnector(
				new io.intino.alexandria.jms.ConnectionConfig("failover:(tcp://localhost:63000)", user, password, clientId),
				new File("temp/cache")
		);
		jmsConnector.start();
		return jmsConnector;
	}

	public static class MyJmsConnector extends JmsConnector {

		public MyJmsConnector(ConnectionConfig config, File outboxDirectory) {
			super(config, outboxDirectory);
		}

		@Override
		public void requestResponse(String path, javax.jms.Message message, Consumer<Message> onResponse) {
			if (session() == null) {
				Logger.error("Connection lost. Invalid session");
				return;
			}
			try {
				QueueProducer producer = new QueueProducer(session(), path);
				TemporaryQueue temporaryQueue = session().createTemporaryQueue();
				javax.jms.MessageConsumer consumer = session().createConsumer(temporaryQueue);
				consumer.setMessageListener(m -> acceptMessage(onResponse, consumer, m));
				message.setJMSReplyTo(temporaryQueue);
				message.setJMSCorrelationID(createRandomString());
				sendMessage(producer, message, 10000);
				producer.close();
			} catch (JMSException e) {
				Logger.error(e);
			}
		}

		private boolean sendMessage(JmsProducer producer, javax.jms.Message message, int expirationTimeInSeconds) {
			final boolean[] result = {false};
			try {
				Thread thread = new Thread(() -> result[0] = producer.produce(message, expirationTimeInSeconds));
				thread.start();
				thread.join(1000);
				thread.interrupt();
			} catch (InterruptedException ignored) {
			}
			return result[0];
		}

		private void acceptMessage(Consumer<javax.jms.Message> onResponse, javax.jms.MessageConsumer consumer, javax.jms.Message m) {
			try {
				onResponse.accept(m);
				consumer.close();
			} catch (JMSException e) {
				Logger.error(e);
			}
		}
	}
}
