package io.intino.ness.tcp;

import io.intino.ness.core.Datalake;

import javax.jms.Session;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class ActiveMQSetStore implements Datalake.SetStore {
	private final Map<String, Tank> tanks;
	private final ActiveMQDatalake.Connection connection;
	private AdminService adminService;
	private PumpService pumpService;
	private Session session;

	public ActiveMQSetStore(ActiveMQDatalake.Connection connection) {
		this.connection = connection;
		this.tanks = new HashMap<>();

	}

	@Override
	public Stream<Tank> tanks() {
		return tanks.values().stream();
	}

	@Override
	public Tank tank(String name) {
		Tank tank = new ActiveMQSetTank(name, pumpService);
		tanks.put(name, tank);
		return tank;
	}

	public void open() {
		this.session = connection.session();
		this.adminService = new AdminService(session);
		this.pumpService = new PumpService(session);
	}
}
