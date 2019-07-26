package io.intino.ness.datahub.box;

import io.intino.alexandria.jmx.JMXServer;

import io.intino.alexandria.core.Box;
import java.util.HashMap;
import java.util.Map;

public class JMXManager {

	public JMXServer init(Box box) {
		JMXServer server = new JMXServer(mbClasses(box));
		server.init();
		return server;
	}

	private Map<String, Object[]> mbClasses(Box box) {
		Map<String, Object[]> map = new HashMap<>();
		map.put("io.intino.ness.datahub.box.jmx.Manager", new Object[]{box});
		return map;
	}
}