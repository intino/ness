package io.intino.ness.box.actions;

import io.intino.ness.box.NessBox;


public class AddUserAction {

	public NessBox box;
	public String name;

	public String execute() {
		String password = box.busManager().addUser(name);
		if (password == null) return "User already exists";
		return "User *" + name + "* added with password `" + password + "`";

	}
}