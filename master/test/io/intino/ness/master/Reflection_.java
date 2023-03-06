package io.intino.ness.master;

import io.intino.ness.master.model.Concept.Attribute;
import io.intino.ness.master.model.Entity;
import io.intino.ness.master.reflection.*;

import java.util.List;
import java.util.Optional;

@SuppressWarnings("all")
public class Reflection_ {

	public static void main(String[] args) {
		Datamart datamart = theMasterDatamart();

		Entity entity = datamart.get("id");

		List<Attribute> attributes = entity.attributes();

		String name = entity.attribute("name").value().as(String.class);
		int age = entity.attribute("age").value().as(int.class);
		List<Double> list = entity.attribute("list").value().<List<Double>>as();

		// ENTITY DEF

		EntityDefinition entityDefinition = entity.getDefinition();
		List<AttributeDefinition> allAttributes = entityDefinition.attributes();
		List<AttributeDefinition> attributesDeclaredOnlyInThisClass = entityDefinition.declaredAttributes();

		if(entity.instanceOf(entityDefinition)) {
			// ...
		}

		EntityDefinition otherDefinition = entity.getDefinition();
		boolean isAncestor = entityDefinition.isAncestorOf(otherDefinition);
		boolean isDescendant = entityDefinition.isDescendantOf(otherDefinition);

		List<EntityDefinition> ancestors = entityDefinition.ancestors();
		List<EntityDefinition> descendants = entityDefinition.descendants();

		String nameValueFromReflection = entityDefinition.attribute("name")
				.flatMap(attr -> attr.value(entity)).map(v -> v.as(String.class)).orElse(null);

		int ageValueFromReflection = entityDefinition.attribute("age")
				.flatMap(attr -> attr.value(entity)).map(v -> v.as(int.class)).orElse(null);

		// DATAMART DEF

		DatamartDefinition datamartDefinition = datamart.getDefinition();

		Optional<EntityDefinition> entityClass = datamartDefinition.entity("EntityClass");
		Optional<StructDefinition> structClass = datamartDefinition.struct("StructClass");

		List<EntityDefinition> list1 = datamartDefinition.entities();
		List<EntityDefinition> list2 = datamartDefinition.entities().of(entityDefinition);
		List<EntityDefinition> list3 = datamartDefinition.entities().instanceOf(entityDefinition);
	}

	private static Datamart theMasterDatamart() {
		return null;
	}
}
