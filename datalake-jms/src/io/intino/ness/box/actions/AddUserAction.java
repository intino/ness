package io.intino.ness.box.actions;

import io.intino.ness.box.NessBox;
import io.intino.ness.graph.User;

import static java.util.Collections.emptyList;


public class AddUserAction {

	public NessBox box;
	public String name;

	public String execute() {
		User user = box.nessGraph().userList(u -> u.name().equals(name)).findFirst().orElse(null);
		if (user != null) return "User already exists";
		String password = box.busService().newUser(name);
		box.nessGraph().create("users").user(name, password, emptyList()).save$();
		return "User *" + name + "* added with password `" + password + "`";

	}
}