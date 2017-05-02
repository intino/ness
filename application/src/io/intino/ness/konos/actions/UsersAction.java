package io.intino.ness.konos.actions;

import io.intino.ness.DatalakeManager;
import io.intino.ness.konos.NessBox;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class UsersAction {

	public NessBox box;


	public List<String> execute() {
		Map<String, List<String>> usersMap = datalake().users();
		List<String> users = new ArrayList<>();
		for (String user : usersMap.keySet()) {
			StringBuilder builder = new StringBuilder().append(user);
			List<String> groups = usersMap.get(user);
			if (!groups.isEmpty()) builder.append(" {").append(String.join(" ", groups)).append("}");
			users.add(builder.toString());
		}
		return users;
	}

	private DatalakeManager datalake() {
		return box.get(DatalakeManager.class);
	}


}