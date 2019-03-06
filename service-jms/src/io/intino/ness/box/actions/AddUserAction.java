package io.intino.ness.box.actions;

import io.intino.ness.box.NessBox;
import io.intino.ness.graph.User;


public class AddUserAction {

	public NessBox box;
	public String name;

	public String execute() {
		User user = box.graph().userList(u -> u.name().equals(name)).findFirst().orElse(null);
		if (user != null) return "User already exists";
		String password = box.busService().newUser(name);
		box.graph().create("users").user(name, password).save$();
		return "User *" + name + "* added with password `" + password + "`";

	}
}