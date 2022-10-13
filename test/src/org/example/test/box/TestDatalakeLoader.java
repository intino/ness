package org.example.test.box;

import io.intino.ness.master.data.AbstractDatalakeLoader;
import io.intino.ness.master.data.ComponentAttributeDefinition;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TestDatalakeLoader extends AbstractDatalakeLoader {

	@Override
	protected Map<String, List<ComponentAttributeDefinition>> initComponentsByEntityType() {
		return new HashMap<>() {{
			put("order", List.of(
					new ComponentAttributeDefinition("checklist", "check", ComponentAttributeDefinition.Type.List),
					new ComponentAttributeDefinition("singleCheck", "check", ComponentAttributeDefinition.Type.Reference)
			));
		}};
	}

	@Override
	protected Set<String> initTypesWithComponents() {
		return Set.of("order");
	}

	@Override
	protected Set<String> initComponentTypes() {
		return Set.of("check");
	}
}
