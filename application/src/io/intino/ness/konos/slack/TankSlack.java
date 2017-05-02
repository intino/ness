package io.intino.ness.konos.slack;

import io.intino.konos.slack.Bot.MessageProperties;
import io.intino.ness.DatalakeManager;
import io.intino.ness.Function;
import io.intino.ness.Tank;
import io.intino.ness.konos.NessBox;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static io.intino.ness.konos.slack.Helper.findTank;
import static io.intino.ness.konos.slack.Helper.ness;

public class TankSlack {
	private static final String OK = ":ok_hand:";

	private NessBox box;

	public TankSlack(NessBox box) {
		this.box = box;
	}

	public void init(com.ullink.slack.simpleslackapi.SlackSession session) {

	}

	public String tag(MessageProperties properties, String[] tags) {
		Tank tank = findTank(box, properties.context().getObjects()[0]);
		tank.tags().clear();
		Collections.addAll(tank.tags(), tags);
		return OK;
	}

	public String rename(MessageProperties properties, String name) {
		Tank tank = findTank(box, properties.context().getObjects()[0]);
		if (tank == null) return "Please select a tank";
		return box.get(DatalakeManager.class).rename(tank, name) ? OK : "Impossible to rename tank";
	}

	public String seal(MessageProperties properties) {
		Tank tank = findTank(box, properties.context().getObjects()[0]);
		datalake().seal(tank);
		return OK;
	}


	public String reflow(MessageProperties properties) {
		Tank tank = findTank(box, properties.context().getObjects()[0]);
		if (tank == null) return "Tank not found";
		DatalakeManager manager = datalake();
		manager.reflow(tank);
		return OK;
	}

	public String migrate(MessageProperties properties, String[] args) {
		List<Function> functions = Arrays.stream(args).map(a -> ness(box).functionList().stream().filter(f -> f.name().equals(a)).findFirst().orElse(null)).collect(Collectors.toList());
		if (functions.contains(null)) return "Function " + args[functions.indexOf(null)] + " not found. Please, register it first";
		Tank tank = findTank(box, properties.context().getObjects()[0]);
		String newTankName = nextVersionOf(tank);
		Tank newTank = ness(box).create("tanks", newTankName).tank(newTankName);
		try {
			datalake().migrate(tank, newTank, functions);
		} catch (Exception e) {
			return "Migration failed: " + e.getMessage();
		}
		newTank.save();
		return OK;
	}

	private String nextVersionOf(Tank tank) {
		return tank.qualifiedName().replace("." + tank.version(), "." + (tank.version() + 1));
	}


	private DatalakeManager datalake() {
		return box.get(DatalakeManager.class);
	}
}