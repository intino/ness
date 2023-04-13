package io.intino.ness.datahubterminalplugin.master;


import io.intino.datahub.model.*;
import io.intino.itrules.Frame;
import io.intino.itrules.FrameBuilder;
import io.intino.magritte.framework.Node;

import java.util.*;
import java.util.stream.Collectors;

import static io.intino.itrules.formatters.StringFormatters.firstUpperCase;
import static io.intino.ness.datahubterminalplugin.Formatters.firstUpperCase;
import static io.intino.ness.datahubterminalplugin.Formatters.javaValidName;

public class EntityFrameFactory implements ConceptRenderer {
	private static final String DOT = ".";

	private final String workingPackage;
	private final Datamart datamart;

	public EntityFrameFactory(String workingPackage, Datamart datamart) {
		this.workingPackage = workingPackage;
		this.datamart = datamart;
	}

	public Map<String, Frame> create(Entity entity) {
		Map<String, Frame> map = new HashMap<>(2);
		map.put(calculateEntityPath(entity, workingPackage), frameOf(entity).toFrame());
		if (entity.isDecorable())
			map.put(calculateDecorableEntityPath(entity.core$(), workingPackage), frameOf(entity).add("decorable").toFrame());
		return map;
	}

	private FrameBuilder frameOf(Entity entity) {
		FrameBuilder builder = new FrameBuilder("entity", "class")
				.add("package", workingPackage)
				.add("datamart", datamart.name$())
				.add("name", entity.core$().name())
				.add("expression", entity.methodList().stream().map(m -> ExpressionHelper.exprFrameOf(m, workingPackage)).toArray(Frame[]::new))
				.add("struct", entity.structList().stream().map(s -> structFrame(s, entity)).toArray(Frame[]::new));

		List<Frame> attributes = attributesOf(entity).stream().map(this::attrFrameOf).map(FrameBuilder::toFrame).collect(Collectors.toList());

		entity.structList().stream().map(struct -> attrFrameOf(attrOf(entity.core$(), struct)).toFrame()).forEach(attributes::add);

		builder.add("attribute", attributes.toArray(Frame[]::new));

		if (!datamart.structList().isEmpty()) builder.add("hasStructs", new FrameBuilder().add("package", workingPackage));

		Parameter parent = parameter(entity.core$(), "entity");
		builder.add("parent", parent != null ? withFullPackage(((Entity) parent.values().get(0)).name$()) : baseEntityName());
		builder.add("normalizeId", new FrameBuilder("normalizeId", (entity.isAbstract() || entity.isDecorable()) ? "abstract" : "").add("package", workingPackage).add("name", entity.name$()).toFrame());
		builder.add("isAbstract", entity.isAbstract() ? " abstract" : "");

		return builder;
	}

	private String withFullPackage(String parent) {
		return entitiesPackage() + parent;
	}

	private String baseEntityName() {
		return workingPackage + "." + firstUpperCase(datamart.name$()) + "Entity";
	}

	private Frame structFrame(Struct struct, Entity owner) {
		StructFrameFactory frameFactory = new StructFrameFactory(datamart, workingPackage);
		Map<String, Frame> frames = frameFactory.create(struct, owner.name$());
		return frames.values().stream().findFirst().get();
	}

	private String calculateEntityPath(Entity entity, String thePackage) {
		return thePackage + DOT + "entities" + DOT
				+ (entity.isDecorable() ? "Abstract" : "")
				+ firstUpperCase().format(javaValidName().format(entity.core$().name()).toString());
	}

	private String calculateDecorableEntityPath(Node node, String aPackage) {
		return aPackage + DOT + "entities" + DOT + firstUpperCase().format(javaValidName().format(node.name()).toString());
	}

	@Override
	public Datamart datamart() {
		return datamart;
	}

	@Override
	public String workingPackage() {
		return workingPackage;
	}
}
