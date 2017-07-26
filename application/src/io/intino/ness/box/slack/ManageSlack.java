package io.intino.ness.box.slack;

import io.intino.konos.slack.Bot.MessageProperties;
import io.intino.ness.DatalakeManager;
import io.intino.ness.box.NessBox;
import io.intino.ness.bus.BusManager;
import io.intino.ness.graph.NessGraph;
import io.intino.ness.graph.Tank;

import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Arrays.copyOfRange;
import static java.util.stream.Collectors.toList;

public class ManageSlack {

	private static final String OK = ":ok_hand:";
	private NessBox box;

	public ManageSlack(NessBox box) {
		this.box = box;
	}

	public void init(com.ullink.slack.simpleslackapi.SlackSession session) {
	}

	public String addUser(MessageProperties properties, String name, String[] groups) {
		String password = bus().addUser(name, groups.length == 0 ? Collections.emptyList() : asList(copyOfRange(groups, 1, groups.length)));
		if (password == null) return "User already exists";
		return "User *" + name + "* added with password `" + password + "`";
	}

	public String removeUser(MessageProperties properties, String name) {
		return bus().removeUser(name) ? OK : "User not found";
	}

	public String addTank(MessageProperties properties, String tank) {
		NessGraph ness = ness();
		List<Tank> tanks = ness.tankList(t -> t.name$().equals(tank)).collect(toList());
		if (!tanks.isEmpty()) return "Tank already exist";
		String name = tank.replaceFirst("feed\\.", "");
		Tank newTank = ness.create("tanks").tank(name);
		datalake().registerTank(newTank);
		datalake().feedFlow(newTank);
		newTank.save$();
		return OK;
	}

	public String removeTank(MessageProperties properties, String name) {
		NessGraph wrapper = ness();
		List<Tank> tanks = wrapper.tankList(t -> t.qualifiedName().equals(name)).collect(toList());
		if (tanks.isEmpty()) return "Tank not found";
		for (Tank tank : tanks) {
			datalake().removeTank(tank);
			tank.delete$();
		}
		return OK;
	}

	private NessGraph ness() {
		return box.ness();
	}

	private DatalakeManager datalake() {
		return box.datalakeManager();
	}

	private BusManager bus() {
		return box.busManager();
	}
}