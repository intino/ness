package io.intino.ness.box.slack;

import io.intino.konos.slack.Bot;
import io.intino.konos.slack.Bot.MessageProperties;
import io.intino.ness.box.NessBox;
import io.intino.ness.box.actions.*;

import java.util.Arrays;

public class ManageSlack {

	private static final String OK = ":ok_hand:";
	private NessBox box;

	public ManageSlack(NessBox box) {
		this.box = box;
	}

	public void init(com.ullink.slack.simpleslackapi.SlackSession session) {
	}

	public String addUser(MessageProperties properties, String name) {
		AddUserAction action = new AddUserAction();
		action.box = box;
		action.name = name;
		return action.execute();
	}

	public String removeUser(MessageProperties properties, String name) {
		RemoveUserAction action = new RemoveUserAction();
		action.box = box;
		action.name = name;
		return action.execute();
	}

	public String addTank(MessageProperties properties, String tank) {
		AddTankAction action = new AddTankAction();
		action.box = box;
		action.name = tank;
		return action.execute();
	}

	public String removeTank(MessageProperties properties, String name) {
		RemoveTankAction action = new RemoveTankAction();
		action.box = box;
		action.name = name;
		return action.execute();
	}

	public String addFunction(MessageProperties properties) {
		Bot.BotFile file = properties.file();
		AddFunctionAction action = new AddFunctionAction();
		action.box = box;
		action.name = file.name().replace(".java", "");
		action.code = file.textContent();
		return action.execute();
	}

	public String removeFunction(MessageProperties properties, String name) {
		RemoveFunctionAction action = new RemoveFunctionAction();
		action.box = box;
		action.name = name;
		return action.execute();
	}

	public Object addExternalBus(MessageProperties properties, String name, String externalBusUrl, String user, String password) {
		AddExternalBusAction action = new AddExternalBusAction();
		action.box = box;
		action.name = name;
		action.externalBusUrl = externalBusUrl.replaceAll("<|>", "");
		action.user = user;
		action.password = password;
		return action.execute();
	}

	public String removeExternalBus(MessageProperties properties, String name) {
		RemoveExternalBusAction action = new RemoveExternalBusAction();
		action.box = box;
		action.name = name;
		return action.execute();
	}

	public String addJmsConnector(MessageProperties properties, String name, String externalBus, String direction, String[] args) {
		String[] topics = Arrays.copyOfRange(args, 3, args.length);
		AddJmsConnectorAction action = new AddJmsConnectorAction();
		action.box = box;
		action.name = name;
		action.externalBus = externalBus;
		action.direction = direction;
		action.topics = String.join(" ", topics);
		return action.execute();
	}

	public String removeJmsConnector(MessageProperties properties, String name) {
		RemoveJmsConnectorAction action = new RemoveJmsConnectorAction();
		action.box = box;
		action.name = name;
		return action.execute();
	}

	public String pipes(MessageProperties properties) {
		PipesAction action = new PipesAction();
		action.box = box;
		return action.execute();
	}
}