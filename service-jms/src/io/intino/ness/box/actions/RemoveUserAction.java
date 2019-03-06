package io.intino.ness.box.actions;

import io.intino.ness.box.NessBox;
import io.intino.ness.graph.User;

import static io.intino.ness.box.actions.Action.OK;


public class RemoveUserAction {

	public NessBox box;
	public String name;

	public String execute() {
		User user = box.graph().userList(u -> u.name().equals(name)).findFirst().orElse(null);
		if (user == null) return "User not found";
		user.delete$();
		box.busService().removeUser(name);
		return OK;
	}


}