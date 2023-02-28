package io.intino.ness.master.util;

import io.intino.ness.master.Entity;

import java.util.Collections;
import java.util.List;

public class EntityUtils {

	public static List<Entity.Attribute> differential(Entity a, Entity b) {
		if(a == null && b == null) return Collections.emptyList();
		if(a != null && b == null) return a.attributes();
		if(a == null) return b.attributes();
		if(!a.getClass().equals(b.getClass())) return null;
		List<Entity.Attribute> attributes = a.attributes();
		attributes.removeAll(b.attributes());
		return attributes;
	}
}
