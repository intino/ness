package io.intino.ness.datahubterminalplugin.master;


import io.intino.datahub.model.Datamart;
import io.intino.datahub.model.Entity;
import io.intino.itrules.Frame;
import io.intino.itrules.FrameBuilder;

import java.util.HashMap;
import java.util.Map;

import static io.intino.itrules.formatters.StringFormatters.firstUpperCase;
import static io.intino.ness.datahubterminalplugin.Formatters.javaValidName;

public class EntityMounterFrameFactory implements ConceptRenderer {
	private static final String DOT = ".";

	private final String destinationPackage;
	private final String ontologyPackage;
	private final Datamart datamart;

	public EntityMounterFrameFactory(String destinationPackage, String ontologyPackage, Datamart datamart) {
		this.destinationPackage = destinationPackage;
		this.ontologyPackage = ontologyPackage;
		this.datamart = datamart;
	}

	public Map<String, Frame> create(Entity entity) {
		if(entity.isAbstract()) return new HashMap<>(0);
		return Map.of(getMounterPath(entity, destinationPackage), frameOf(entity).toFrame());
	}

	private FrameBuilder frameOf(Entity entity) {
		FrameBuilder builder = new FrameBuilder("mounter")
				.add("message")
				.add("package", destinationPackage)
				.add("ontologypackage", ontologyPackage)
				.add("datamart", datamart.name$())
				.add("name", entity.core$().name())
				.add("attribute", attributesOf(entity).stream().map(this::attrFrameOf).toArray(FrameBuilder[]::new));
		if (!datamart.structList().isEmpty()) builder.add("hasStructs", new FrameBuilder().add("package", destinationPackage));
		final Parameter parent = parameter(entity.core$(), "entity");
		builder.add("parent", parent != null ? ((Entity) parent.values().get(0)).name$() : "io.intino.ness.master.model.Entity");
		builder.add("normalizeId", new FrameBuilder("normalizeId", (entity.isAbstract() || entity.isDecorable()) ? "abstract" : "").add("package", destinationPackage).add("name", entity.name$()).toFrame());
		if (entity.isDecorable() || entity.isAbstract()) builder.add("isAbstract", "abstract");
		if (entity.isDecorable()) builder.add("abstract", "abstract");
		return builder;
	}

	@Override
	public Datamart datamart() {
		return datamart;
	}

	@Override
	public String workingPackage() {
		return ontologyPackage;
	}

	private String getMounterPath(Entity entity, String aPackage) {
		return aPackage + DOT + "mounters" + DOT + firstUpperCase().format(javaValidName().format(entity.core$().name() + "Mounter").toString());
	}
}
