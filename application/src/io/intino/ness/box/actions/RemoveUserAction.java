package io.intino.ness.box.actions;

import io.intino.ness.box.NessBox;

import static io.intino.ness.box.actions.Action.OK;


public class RemoveUserAction {

	public NessBox box;
	public String name;

	public String execute() {
		return box.busManager().removeUser(name) ? OK : "User not found";
	}


}