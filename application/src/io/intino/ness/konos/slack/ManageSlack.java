package io.intino.ness.konos.slack;

import io.intino.konos.slack.Bot.MessageProperties;
import io.intino.ness.DatalakeManager;
import io.intino.ness.Ness;
import io.intino.ness.Tank;
import io.intino.ness.konos.NessBox;

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Arrays.copyOfRange;

public class ManageSlack {

	private NessBox box;

	public ManageSlack(NessBox box) {
		this.box = box;
	}

	public void init(com.ullink.slack.simpleslackapi.SlackSession session) {

	}

	public String addUser(MessageProperties properties, String name, String[] groups) {
		String password = datalake().addUser(name, asList(copyOfRange(groups, 1, groups.length)));
		if (password == null) return "User already exists";
		return "User *" + name + "* added with password `" + password + "`";
	}

	public String removeUser(MessageProperties properties, String name) {
		return datalake().removeUser(name) ? ":ok:hand:" : "User not found";
	}

	public String addTank(MessageProperties properties, String tank) {
		Ness ness = box.graph().wrapper(Ness.class);
		List<Tank> tanks = ness.tankList(t -> t.name().equals(tank));
		if (!tanks.isEmpty()) return "Tank already exist";
		String name = tank.replaceFirst("feed\\.", "");
		Tank newTank = ness.create("tanks").tank(name);
		datalake().registerTank(newTank);
		datalake().feedFlow(newTank);
		newTank.save();
		return ":ok_hand:";
	}

	public String removeTank(MessageProperties properties, String name) {
		Ness wrapper = box.graph().wrapper(Ness.class);
		List<Tank> tanks = wrapper.tankList(t -> t.name().equals(name));
		if (tanks.isEmpty()) return "Tank not found";
		for (Tank tank : tanks) {
			datalake().removeTank(tank);
			tank.delete();
		}
		return ":ok_hand:";
	}

	private DatalakeManager datalake() {
		return box.get(DatalakeManager.class);
	}
}