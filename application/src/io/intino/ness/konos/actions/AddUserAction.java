package io.intino.ness.konos.actions;

import io.intino.ness.konos.NessBox;


public class AddUserAction extends Action{

	public NessBox box;
	public String name;
	public java.util.List<String> groups;

	public String execute() {
		String password = datalake(box).addUser(name, groups);
		if (password == null) return "User already exists";
		return "User *" + name + "* added with password `" + password + "`";
	}

}