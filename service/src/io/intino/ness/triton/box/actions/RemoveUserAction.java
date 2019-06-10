package io.intino.ness.triton.box.actions;

import io.intino.ness.triton.box.ServiceBox;
import io.intino.ness.triton.graph.User;


public class RemoveUserAction {

	public ServiceBox box;
	public String name;

	public String execute() {
		User user = box.graph().userList(u -> u.name().equals(name)).findFirst().orElse(null);
		if (user == null) return "User not found";
		user.delete$();
		box.busService().removeUser(name);
		return Action.OK;
	}


}