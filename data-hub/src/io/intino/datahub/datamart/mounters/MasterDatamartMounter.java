package io.intino.datahub.datamart.mounters;

import io.intino.alexandria.message.Message;
import io.intino.datahub.box.DataHubBox;
import io.intino.datahub.datamart.MasterDatamart;

public abstract sealed class MasterDatamartMounter permits EntityMounter, ReelMounter, TimelineMounter {

	protected final MasterDatamart datamart;

	public MasterDatamartMounter(MasterDatamart datamart) {
		this.datamart = datamart;
	}

	public DataHubBox box() {
		return datamart.box();
	}

	public abstract void mount(Message message);
}
