package com.cinepolis.master;

import io.intino.master.data.DefaultDatalakeLoader;
import io.intino.master.model.Triple;
import io.intino.master.model.TripleRecord;
import io.intino.master.serialization.MasterSerializer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TestDatalakeLoader extends DefaultDatalakeLoader {

	@Override
	public LoadResult load(File rootDirectory, MasterSerializer serializer) {
		WritableLoadResult result = LoadResult.create();
		loadRecordsFromDisk(rootDirectory, result, serializer);
		return result;
	}

	protected void loadRecordsFromDisk(File rootDirectory, WritableLoadResult result, MasterSerializer serializer) {
		Map<String, TripleRecord> entities = result.records();
		Map<String, TripleRecord> components = new HashMap<>();

		loadFromDisk(rootDirectory, result, entities, components);

		handleComponents(entities, components, serializer);
	}

	private void handleComponents(Map<String, TripleRecord> entities, Map<String, TripleRecord> components, MasterSerializer serializer) {
		for(TripleRecord entity : entities.values()) {
			if(hasComponents(entity)) setEntityComponents(entity, components, serializer);
		}
	}

	private void setEntityComponents(TripleRecord entity, Map<String, TripleRecord> components, MasterSerializer serializer) {
		String entityId = entity.id().substring(0, entity.id().indexOf(':'));
		for(TripleRecord component : components.values()) {
			if(isComponentOfEntity(component, entityId)) {
				addComponentToEntity(component, entity, serializer);
			}
		}
	}

	private void addComponentToEntity(TripleRecord component, TripleRecord entity, MasterSerializer serializer) {
		if(!ComponentsByEntityType.containsKey(entity.type())) return;
		String componentType = component.type();
		for(ComponentAttributeDefinition def : ComponentsByEntityType.get(entity.type())) {
			if(def.component.equals(componentType)) {
				if(def.type == ComponentAttributeDefinition.Type.Reference)
					setEntityComponent(def.name, component, entity, serializer);
				else if(def.type == ComponentAttributeDefinition.Type.List)
					updateEntityComponentList(def.name, component, entity, serializer);
				else if(def.type == ComponentAttributeDefinition.Type.Map)
					updateEntityComponentMap(def.name, component, entity, serializer);
			}
		}
	}

	private void updateEntityComponentMap(String name, TripleRecord component, TripleRecord entity, MasterSerializer serializer) {
		String listSep = ",";// TODO check conflicts
		String attrib = entity.getAttribute(name);

		Map<String, String> map;
		if(attrib == null) map = new HashMap<>(1);
		else map = Arrays.stream(attrib.split(listSep))
				.map(s -> s.split("="))
				.collect(Collectors.toMap(s -> s[0], s -> s[1]));

		map.put(component.id(), serializer.serialize(component.attributes()));

		entity.setAttribute(name, map.entrySet().stream().map(e -> e.getKey() + "=" + e.getValue()).collect(Collectors.joining(listSep)));
	}

	private void updateEntityComponentList(String name, TripleRecord component, TripleRecord entity, MasterSerializer serializer) {
		String listSep = ",";// TODO check conflicts
		String attrib = entity.getAttribute(name);

		List<String> list;
		if(attrib == null) list = new ArrayList<>();
		else list = Arrays.asList(attrib.split(listSep));

		list.add(serializer.serialize(component.attributes()));

		entity.setAttribute(name, String.join(listSep, list));
	}

	private void setEntityComponent(String name, TripleRecord component, TripleRecord entity, MasterSerializer serializer) {
		entity.setAttribute(name, serializer.serialize(component.attributes()));
	}

	private static final Map<String, List<ComponentAttributeDefinition>> ComponentsByEntityType = new HashMap<>() {{
		put("ordertype", List.of(
				new ComponentAttributeDefinition("checklist", "check", ComponentAttributeDefinition.Type.List),
				new ComponentAttributeDefinition("singleCheck", "check", ComponentAttributeDefinition.Type.Reference)
		));
	}};

	private boolean isComponentOfEntity(TripleRecord component, String entityId) {
		return component.id().startsWith(entityId);
	}

	private static final Set<String> TypesWithComponents = Set.of();
	private boolean hasComponents(TripleRecord entity) {
		return TypesWithComponents.contains(entity.type());
	}

	private void loadFromDisk(File rootDirectory, WritableLoadResult result, Map<String, TripleRecord> entities, Map<String, TripleRecord> components) {
		try(Stream<Path> files = Files.walk(rootDirectory.toPath())) {
			files.map(Path::toFile)
					.filter(f -> f.isFile() && f.getName().endsWith(TRIPLES_EXTENSION))
					.flatMap(file -> readTriplesFromFile(file, result))
					.forEach(triple -> register(entities, components, triple));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void register(Map<String, TripleRecord> entities, Map<String, TripleRecord> components, Triple triple) {
		Map<String, TripleRecord> map = isComponent(triple.type()) ? components : entities;
		map.computeIfAbsent(triple.subject(), TripleRecord::new).setAttribute(triple.predicate(), triple.value());
	}

	private static final Set<String> ComponentTypes = Set.of("check");
	private boolean isComponent(String id) {
		return ComponentTypes.contains(Triple.typeOf(id));
	}

	private static class ComponentAttributeDefinition {

		private final String name;
		private final String component;
		private final Type type;

		public ComponentAttributeDefinition(String name, String component, Type type) {
			this.name = name;
			this.component = component;
			this.type = type;
		}

		enum Type {
			Reference, List, Map
		}
	}
}
