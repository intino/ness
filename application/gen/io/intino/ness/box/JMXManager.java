package io.intino.ness.box;

import io.intino.konos.jmx.JMXServer;

import io.intino.konos.alexandria.Box;
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
		map.put("io.intino.ness.box.jmx.Manager", new Object[]{box});
		return map;
	}
}