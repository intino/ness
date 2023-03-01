package io.intino.ness.master;

import io.intino.ness.master.model.Entity;
import io.intino.ness.master.reflection.AttributeDefinition;
import io.intino.ness.master.reflection.DatamartDefinition;
import io.intino.ness.master.reflection.EntityDefinition;

import java.util.List;
import java.util.Optional;

@SuppressWarnings("all")
public class Reflection_ {

	public static void main(String[] args) {
		Datamart datamart = theMasterDatamart();

		Entity entity = datamart.get("");
		entity.attribute("name").value().as(String.class);

		EntityDefinition entityDef = entity.getDefinition();
		Optional<AttributeDefinition> attribute = entityDef.attribute("name");

		DatamartDefinition definition = datamart.getDefinition();

		List<EntityDefinition> list1 = definition.entities();
		List<EntityDefinition> list2 = definition.entities().of(entityDef);
		List<EntityDefinition> list3 = definition.entities().instanceOf(entityDef);
	}

	private static Datamart theMasterDatamart() {
		return null;
	}
}
