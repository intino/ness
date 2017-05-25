package io.intino.ness.box.actions;

import io.intino.ness.box.NessBox;


public class AddUserAction {

	public NessBox box;
	public String name;
	public java.util.List<String> groups;

	public String execute() {
		String password = box.datalakeManager().addUser(name, groups);
		if (password == null) return "User already exists";
		return "User *" + name + "* added with password `" + password + "`";
	}

}