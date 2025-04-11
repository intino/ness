package systems.intino.eventsourcing.datahub.model.rules;

import io.intino.tara.language.model.rules.variable.VariableRule;

public class HasSubjectId implements VariableRule<io.intino.tara.language.model.Mogram> {

	@Override
	public boolean accept(io.intino.tara.language.model.Mogram value) {
		return value.components().stream().anyMatch(c -> c.name().equals("subject"));
	}

	@Override
	public String errorMessage() {
		return "Referenced message must have an attribute named subject";
	}
}