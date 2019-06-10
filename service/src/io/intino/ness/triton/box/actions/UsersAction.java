package io.intino.ness.triton.box.actions;

import io.intino.ness.triton.box.ServiceBox;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class UsersAction {

	public ServiceBox box;


	public List<String> execute() {
		Map<String, List<String>> usersMap = box.busService().users();
		List<String> users = new ArrayList<>();
		for (String user : usersMap.keySet()) {
			StringBuilder builder = new StringBuilder().append(user);
			List<String> groups = usersMap.get(user);
			if (!groups.isEmpty()) builder.append(" {").append(String.join(" ", groups)).append("}");
			users.add(builder.toString());
		}
		return users;
	}
}