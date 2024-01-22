package io.intino.ness.terminal.builder.codegeneration.datamarts;

import io.intino.alexandria.message.Message;
import io.intino.datahub.model.Entity;
import io.intino.datahub.model.Sensor;
import io.intino.datahub.model.Timeline;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

public class TimelineUtils {

	public static Stream<String> types(Timeline t) {
		return asTypes(tanksOf(t));
	}

	public static Stream<String> tanksOf(Timeline t) {
		Set<String> tanks = new HashSet<>();
		String entityTank = tankName(t.entity());
		if (entityTank != null) tanks.add(entityTank);
		if (t.isRaw()) tanks.add(tankName(t.asRaw().tank().sensor()));
		else {
			tanks.addAll(t.asCooked().timeSeriesList().stream().map(ts -> tankName(ts.tank())).toList());
			tanks.addAll(t.asCooked().timeSeriesList().stream().filter(Timeline.Cooked.TimeSeries::isCount).flatMap(ts -> tanksOf(ts.asCount())).toList());
		}
		return tanks.stream();
	}

	private static Stream<String> asTypes(Stream<String> tanks) {
		return tanks.map(TimelineUtils::typeOfTank);
	}

	public static String typeOfTank(String tank) {
		return tank.contains(".") ? tank.substring(tank.lastIndexOf(".") + 1) : tank;
	}

	public static Stream<String> tanksOf(Timeline.Cooked.TimeSeries.Count ts) {
		return ts.operationList().stream().map(d -> tankName(d.tank()));
	}

	private static String tankName(Sensor sensor) {
		return sensor.core$().fullName().replace("$", ".");
	}

	private static String tankName(io.intino.datahub.model.Datalake.Tank.Message tank) {
		return tank.message().core$().fullName().replace("$", ".");
	}


	private static String tankName(Entity e) {
		return e.from() == null ? null : e.from().message().core$().fullName().replace("$", ".");
	}

	private static String valueOf(Message message, Entity.Attribute source) {
		return message.get(source.name$()).asString();
	}

}
