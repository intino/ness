package io.intino.ness.tcp;

import io.intino.alexandria.Timetag;
import io.intino.ness.core.Datalake;

import java.util.stream.Stream;


public class ActiveMQSetTank implements Datalake.SetStore.Tank {

	private final String name;
	private final PumpService service;
	private AdminService adminService;

	ActiveMQSetTank(String name, PumpService service) {
		this.name = name;
		this.service = service;
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public Stream<Tub> tubs() {
		return null;
	}

	@Override
	public Tub first() {
		return null;
	}

	@Override
	public Tub last() {
		return null;
	}

	@Override
	public Tub on(Timetag tag) {
		return null;
	}

	@Override
	public Stream<Tub> tubs(int count) {
		return null;
	}

	@Override
	public Stream<Tub> tubs(Timetag from, Timetag to) {
		return null;
	}
}
