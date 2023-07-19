package io.intino.datahub.box.actions;

import io.intino.alexandria.message.Message;
import io.intino.datahub.box.DataHubBox;
import io.intino.datahub.datamart.MasterDatamart;


public class GetEntityAction {
	public DataHubBox box;
	public io.intino.alexandria.Context context = new io.intino.alexandria.Context();
	public String id;

	public String execute() {
		for (MasterDatamart d : box.datamarts().datamarts()) {
			Message result = d.entityStore().stream().filter(m -> m.get("id").asString().equals(id)).findFirst().orElse(null);
			if (result != null) return result.toString();
		}
		return "null";
	}
}