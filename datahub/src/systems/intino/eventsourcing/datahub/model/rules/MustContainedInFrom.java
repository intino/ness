package systems.intino.eventsourcing.datahub.model.rules;

import io.intino.tara.language.model.rules.variable.VariableRule;

public class MustContainedInFrom implements VariableRule<io.intino.tara.language.model.Mogram> {

	@Override
	public boolean accept(io.intino.tara.language.model.Mogram value) {
		return true; //TODO
	}
}
