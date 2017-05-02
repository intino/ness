package io.intino.ness.konos.actions;

import io.intino.ness.konos.NessBox;


public class RemoveUserAction extends Action {

	public NessBox box;
	public String name;

	public String execute() {
		return datalake(box).removeUser(name) ? OK : "User not found";
	}


}