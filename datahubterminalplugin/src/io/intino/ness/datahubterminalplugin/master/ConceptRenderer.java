package io.intino.ness.datahubterminalplugin.master;

import io.intino.datahub.model.Attribute;
import io.intino.datahub.model.Entity;
import io.intino.datahub.model.Struct;

import java.util.*;
import java.util.stream.Collectors;

public interface ConceptRenderer {

	default List<ConceptAttribute> attributesOf(Struct struct) {
		return struct.attributeList().stream().map(a -> new ConceptAttribute(a, struct.core$())).collect(Collectors.toList());
	}

	default List<ConceptAttribute> attributesOf(Entity entity) {
		Map<String, ConceptAttribute> map = new LinkedHashMap<>();
		if(entity.from() != null) Helper.getAttributesFromEvent(entity, entity.from().message().attributeList(), map);
		Helper.getAttributesFromParents(entity, map);
		Helper.getAttributesFromEntity(entity, entity.attributeList(), map, false);
		return new ArrayList<>(map.values());
	}

	class Helper {
		private static void getAttributesFromEvent(Entity entity, List<Attribute> attributes, Map<String, ConceptAttribute> map) {
			for(Attribute attr : attributes) {
				if(entity.exclude().contains(attr.name$())) continue;
				map.put(attr.name$(), new ConceptAttribute(attr, entity.core$()));
			}
		}

		private static void getAttributesFromParents(Entity entity, Map<String, ConceptAttribute> map) {
			if(!entity.isExtensionOf()) return;
			List<Entity.Attribute> attributes = new ArrayList<>();
			Entity parent = entity.asExtensionOf().entity();
			while(parent != null) {
				attributes.addAll(parent.attributeList());
				parent = parent.isExtensionOf() ? parent.asExtensionOf().entity() : null;
			}
			Collections.reverse(attributes);
			getAttributesFromEntity(entity, attributes, map, true);
		}

		private static void getAttributesFromEntity(Entity entity, List<Entity.Attribute> attributeList, Map<String, ConceptAttribute> attribs, boolean inherited) {
			attributeList.forEach(a -> attribs.put(a.name$(), new ConceptAttribute(a, entity.core$()).inherited(inherited)));
		}
	}
}
