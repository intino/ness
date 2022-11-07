package io.intino.ness.master.data;


import io.intino.ness.master.model.Triplet;
import io.intino.ness.master.model.TripletRecord;
import io.intino.ness.master.serialization.MasterSerializer;

import java.util.*;
import java.util.stream.Collectors;

public class ComponentsTripletsDigester extends DefaultTripletsDigester {

	protected static final String LIST_ENTRY_SEPARATOR = ",";
	protected static final String MAP_ENTRY_SEPARATOR = ",";

	private final Map<String, List<ComponentAttributeDefinition>> componentsByEntityType;
	private final Set<String> typesWithComponents;
	private final Set<String> componentTypes;

	public ComponentsTripletsDigester(Map<String, List<ComponentAttributeDefinition>> componentsByEntityType,
									  Set<String> typesWithComponents,
									  Set<String> componentTypes) {
		this.componentsByEntityType = componentsByEntityType;
		this.typesWithComponents = typesWithComponents;
		this.componentTypes = componentTypes;
	}

	@Override
	public Result load(TripletLoader tripletLoader, MasterSerializer serializer) throws Exception {
		WritableResult result = Result.create();

		Map<String, TripletRecord> entities = result.records();
		Map<String, TripletRecord> components = new HashMap<>();

		tripletLoader.loadTriplets(result.stats()).forEach(t -> register(entities, components, t));

		handleComponents(entities, components, serializer);

		return result;
	}

	private void register(Map<String, TripletRecord> entities, Map<String, TripletRecord> components, Triplet triplet) {
		Map<String, TripletRecord> map = isComponent(triplet.type()) ? components : entities;
		map.computeIfAbsent(triplet.subject(), TripletRecord::new).put(triplet);
	}

	private void handleComponents(Map<String, TripletRecord> entities, Map<String, TripletRecord> components, MasterSerializer serializer) {
		for(TripletRecord entity : entities.values()) {
			if(hasComponents(entity)) setEntityComponents(entity, components, serializer);
		}
	}

	private void setEntityComponents(TripletRecord entity, Map<String, TripletRecord> components, MasterSerializer serializer) {
		String entityId = entity.id().substring(0, entity.id().indexOf(':'));
		for(TripletRecord component : components.values()) {
			if(isComponentOfEntity(component, entityId)) {
				addComponentToEntity(component, entity, serializer);
			}
		}
	}

	private void addComponentToEntity(TripletRecord component, TripletRecord entity, MasterSerializer serializer) {
		if(!componentsByEntityType.containsKey(entity.type())) return;
		String componentType = component.type();
		for(ComponentAttributeDefinition def : componentsByEntityType.get(entity.type())) {
			if(def.component().equals(componentType)) {
				if(def.type() == ComponentAttributeDefinition.Type.Reference)
					setEntityComponent(def.name(), component, entity, serializer);
				else if(def.type() == ComponentAttributeDefinition.Type.List)
					updateEntityComponentList(def.name(), component, entity, serializer);
				else if(def.type() == ComponentAttributeDefinition.Type.Map)
					updateEntityComponentMap(def.name(), component, entity, serializer);
			}
		}
	}

	private void updateEntityComponentMap(String name, TripletRecord component, TripletRecord entity, MasterSerializer serializer) {
		String attrib = entity.getValue(name);

		Map<String, String> map;
		if(attrib == null) map = new HashMap<>(1);
		else map = Arrays.stream(attrib.split(MAP_ENTRY_SEPARATOR))
				.map(s -> s.split("="))
				.collect(Collectors.toMap(s -> s[0], s -> s[1]));

		map.put(component.id(), serializer.serialize(component));

		entity.put(new Triplet(entity.id(), name, map.entrySet().stream().map(e -> e.getKey() + "=" + e.getValue()).collect(Collectors.joining(MAP_ENTRY_SEPARATOR))));
	}

	private void updateEntityComponentList(String name, TripletRecord component, TripletRecord entity, MasterSerializer serializer) {
		String attrib = entity.getValue(name);

		List<String> list;
		if(attrib == null) list = new ArrayList<>();
		else list = Arrays.asList(attrib.split(LIST_ENTRY_SEPARATOR));

		list.add(serializer.serialize(component));

		entity.put(new Triplet(entity.id(), name, String.join(LIST_ENTRY_SEPARATOR, list)));
	}

	private void setEntityComponent(String name, TripletRecord component, TripletRecord entity, MasterSerializer serializer) {
		entity.put(new Triplet(entity.id(), name, serializer.serialize(component)));
	}

	private boolean isComponentOfEntity(TripletRecord component, String entityId) {
		return component.id().startsWith(entityId);
	}

	private boolean hasComponents(TripletRecord entity) {
		return typesWithComponents.contains(entity.type());
	}

//	private static final Set<String> ComponentTypes = Set.of("check");
	private boolean isComponent(String id) {
		return componentTypes.contains(Triplet.typeOf(id));
	}
}
