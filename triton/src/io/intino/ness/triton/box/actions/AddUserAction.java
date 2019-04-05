package io.intino.ness.triton.box.actions;

import io.intino.ness.triton.box.TritonBox;
import io.intino.ness.triton.graph.User;


public class AddUserAction {

	public TritonBox box;
	public String name;

	public String execute() {
		User user = box.graph().userList(u -> u.name().equals(name)).findFirst().orElse(null);
		if (user != null) return "User already exists";
		String password = box.busService().newUser(name);
		box.graph().create("users").user(name, password).save$();
		return "User *" + name + "* added with password `" + password + "`";

	}
}