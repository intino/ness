package io.intino.datahub.datamart.mounters;

import io.intino.alexandria.message.Message;
import io.intino.datahub.datamart.MasterDatamart;

public final class ReelMounter extends MasterDatamartMounter {

	public ReelMounter(MasterDatamart datamart) {
		super(datamart);
	}

	@Override
	public void mount(Message message) {
		if (message == null) return;
		// TODO
	}
}
